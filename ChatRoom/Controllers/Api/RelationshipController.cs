using ChatRoom.Data;
using ChatRoom.Models.DB;
using CloudinaryDotNet;
using CloudinaryDotNet.Actions;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using System.Text.Json;
using System.Text.RegularExpressions;

namespace ChatRoom.Controllers.Api
{
	[Authorize]
	[Route("api/relationship")]
	[ApiController]
	public class RelationshipController : ControllerBase
	{
		private IConfiguration Configuration;

		private readonly ApplicationDbContext _context;

		private readonly Cloudinary _cloudinary;

		public RelationshipController(IConfiguration configuration, ApplicationDbContext context)
		{
			Configuration = configuration;

			_context = context;

			_cloudinary = new Cloudinary(new Account(Configuration.GetSection("Cloudinary").GetValue<string>("Cloud")!, Configuration.GetSection("Cloudinary").GetValue<string>("ApiKey")!, Configuration.GetSection("Cloudinary").GetValue<string>("ApiSecret")!));
		}

		// GET: api/relationship/users
		// Provide all users.
		[HttpGet("users")]
		public async Task<ActionResult<IEnumerable<UserModel>>> GetUsers()
		{
			IEnumerable<UserModel> users = _context.Users!.ToList();

			foreach(var user in users)
			{
				var resource = await _cloudinary.GetResourceAsync("chatroom/users/" + user.Id.ToString());
				
				if (resource.SecureUrl != null)
					user.Image = resource.SecureUrl;
			}

			return Ok(users);
		}

		// GET: api/relationship/users/{id}
		// Provide user by id.
		[HttpGet("users/{id:int}")]
		public async Task<ActionResult<UserModel>> GetUser([FromRoute] int id)
		{
			var user = await _context.Users!.FirstOrDefaultAsync(x => x.Id == id);

			var resource = await _cloudinary.GetResourceAsync("chatroom/users/" + user!.Id.ToString());
			
			if (resource.SecureUrl != null)
				user.Image = resource.SecureUrl;

			return Ok(user);
		}

		// GET: api/relationship/contacts
		// Provide relationship contacts.
		[HttpGet("contacts")]
		public async Task<ActionResult<object>> GetContacts()
		{
			var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

			IEnumerable<RelationshipUserModel> relationshipUsers = _context.RelationshipUsers!.ToList();

			var contactUsers =
				(from u in (from relationshipUser in relationshipUsers
							where (relationshipUser.IdUserFirst == userId || relationshipUser.IdUserSecond == userId)
							select relationshipUser)
				 select _context.Users!.First(x => x.Id != userId && (x.Id == u.IdUserFirst || x.Id == u.IdUserSecond)));

			foreach (var user in contactUsers)
			{
				var lastMessage = await _context.MessageUsers!.OrderBy(x => x.Id).LastOrDefaultAsync(x => (x.IdUserFrom == userId && x.IdUserTo == user.Id) || (x.IdUserFrom == user.Id && x.IdUserTo == userId));

				if (lastMessage != null)
				{
					user.LastMessage = lastMessage;
					user.LastMessage.Content = lastMessage.Content!.Length < 20 ? lastMessage.Content! : lastMessage.Content!.Substring(0, 20) + " ...";
				}

				var resource = await _cloudinary.GetResourceAsync("chatroom/users/" + user!.Id.ToString());
				
				if (resource.SecureUrl != null)
					user.Image = resource.SecureUrl;
			}

            IEnumerable<RelationshipGroupModel> relationshipGroups = _context.RelationshipGroups!.ToList();

			var contactGroups =
				(from g in (from relationshipGroup in relationshipGroups
							where relationshipGroup.IdUser == userId
							select relationshipGroup)
				 select _context.Groups!.First(x => x.Id == g.IdGroup));

			foreach (var group in contactGroups)
			{
				var lastMessage = await _context.MessageGroups!.OrderBy(x => x.Id).LastOrDefaultAsync(x => x.IdGroup == group.Id);

				if (lastMessage != null)
				{
					group.LastMessage = lastMessage;
					group.LastMessage.Content = lastMessage.Content!.Length < 20 ? lastMessage.Content! : lastMessage.Content!.Substring(0, 20) + " ...";
				}

				var resource = await _cloudinary.GetResourceAsync("chatroom/groups/" + group!.Id.ToString());

				if (resource.SecureUrl != null)
					group.Image = resource.SecureUrl;
			}

			return new ObjectResult(new { users = contactUsers, groups = contactGroups }) { StatusCode = 200 };
		}

		// POST: api/relationship/contact
		// Create a new relationship between two users.
		[HttpPost("contact")]
		public async Task<ActionResult<UserModel>> CreateContact([FromBody] JsonElement body)
		{
			try
			{
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());
				int secondUserId = body.GetProperty("user_id").GetInt32();

				if (userId == secondUserId)
					return new ObjectResult(new { status = 403, message = "You cannot create a contact with your self." }) { StatusCode = 403 };

				var firstUser = await _context.Users!.FirstOrDefaultAsync(x => x.Id == userId);

				if (firstUser == null)
					return new ObjectResult(new { status = 403, message = "There is no user with this first id." }) { StatusCode = 403 };

				var secondUser = await _context.Users!.FirstOrDefaultAsync(x => x.Id == secondUserId);

				if (secondUser == null)
					return new ObjectResult(new { status = 403, message = "There is no user with this second id." }) { StatusCode = 403 };

				var relationshipUsers = await _context.RelationshipUsers!.FirstOrDefaultAsync(x => (x.IdUserFirst == userId && x.IdUserSecond == secondUserId) || (x.IdUserFirst == secondUserId && x.IdUserSecond == userId));

				if (relationshipUsers != null)
					return new ObjectResult(new { status = 403, message = "There is a relationship between these two users." }) { StatusCode = 403 };

				await _context.Database.ExecuteSqlRawAsync(string.Format("INSERT INTO RelationshipUsers VALUES ({0}, {1})", userId, secondUserId));

				return new ObjectResult(new { status = 200, message = "The relationship was created." }) { StatusCode = 200 };
			}
			catch (DbUpdateConcurrencyException ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}

		// GET: api/relationship/group/{groupId}
		// Provide all members from a group.
		[HttpGet("group/{groupId:int}")]
		public async Task<ActionResult<object>> GetMemmbersFromGroup([FromRoute] int groupId)
		{
			var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

			IEnumerable<RelationshipGroupModel> relationshipGroups = _context.RelationshipGroups!.ToList();

			var members =
				(from u in (from relationshipGroup in relationshipGroups
							where relationshipGroup.IdGroup == groupId
							select relationshipGroup)
				 select _context.Users!.First(x => x.Id == u.IdUser));

			foreach(var member in members)
			{
				var resource = await _cloudinary.GetResourceAsync("chatroom/users/" + member!.Id.ToString());

				if (resource.SecureUrl != null)
					member.Image = resource.SecureUrl;
			}

			return Ok(members);
		}

		// POST: api/relationship/group
		// Create a new group.
		[HttpPost("group")]
		public async Task<ActionResult<UserModel>> CreateGroup([FromBody] JsonElement body)
		{
			try
			{
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());
				string name = body.GetProperty("name").ToString();

				var existGroup = await _context.Groups!.FirstOrDefaultAsync(x => x.Name == name);

				if (existGroup != null)
					return new ObjectResult(new { status = 403, message = "There is a group with this name." }) { StatusCode = 403 };

				_context.Groups!.Add(new GroupModel() { Name = name, IdAdminUser = userId });
				await _context.SaveChangesAsync();

				var group = await _context.Groups.FirstOrDefaultAsync(x => x.Name == name && x.IdAdminUser == userId);

				if (group != null)
					await _context.Database.ExecuteSqlRawAsync(string.Format("INSERT INTO RelationshipGroups VALUES ({0}, {1})", userId, group.Id));

				return Ok(group);
			}
			catch (DbUpdateConcurrencyException ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}

		// PUT: api/relationship/group/{groupId}
		// Update profile group.
		[HttpPut("group/{groupId:int}")]
		public async Task<IActionResult> UpdateProfileGroup([FromRoute] int groupId, [FromBody] JsonElement body)
		{
			try
			{
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());
				string name = body.GetProperty("name").ToString();

				var group = await _context.Groups!.FirstAsync(x => x.Id == groupId);

				if (group == null)
					return new ObjectResult(new { status = 403, message = "There is no group with this id." }) { StatusCode = 403 };

				if (group.IdAdminUser != userId)
					return new ObjectResult(new { status = 403, message = "This group is managed by another user. You are not the administrator." }) { StatusCode = 403 };

				group.Name = name;

				_context.Entry(group!).State = EntityState.Modified;
				await _context.SaveChangesAsync();

				return new ObjectResult(new { status = 200, message = "The profile group has been changed successfully." }) { StatusCode = 200 };
			}
			catch (Exception ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}

		// PUT: api/relationship/group/{groupId}/profile-image
		// Update profile group image.
		[HttpPut("group/{groupId:int}/profile-image")]
		public async Task<IActionResult> UpdateProfileGroupImage([FromRoute] int groupId, IFormFile image)
		{
			try
			{
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

				var group = await _context.Groups!.FirstAsync(x => x.Id == groupId);

				if (group == null)
					return new ObjectResult(new { status = 403, message = "There is no group with this id." }) { StatusCode = 403 };

				if (group.IdAdminUser != userId)
					return new ObjectResult(new { status = 403, message = "This group is managed by another user. You are not the administrator." }) { StatusCode = 403 };

				using (var stream = new MemoryStream())
				{
					await image.CopyToAsync(stream);
					stream.Seek(0, SeekOrigin.Begin);

					var uploadParams = new ImageUploadParams()
					{
						File = new FileDescription(image.FileName, stream),
						PublicId = groupId.ToString(),
						Folder = "chatroom/groups",
						Overwrite = true
					};

					var resource = await _cloudinary.UploadAsync(uploadParams);

					return Ok(resource.SecureUrl);
				}
			}
			catch (Exception ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}

		// PATCH: api/relationship/group/{groupId}
		// Add a new member into a group.
		[HttpPatch("group/{groupId:int}")]
		public async Task<ActionResult<UserModel>> AddMemeberToGroup([FromRoute] int groupId, [FromBody] JsonElement body)
		{
			try
			{
				var adminUserId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());
				int userId = body.GetProperty("user_id").GetInt32();

				var existGroup = await _context.Groups!.FirstOrDefaultAsync(x => x.Id == groupId);

				if (existGroup == null)
					return new ObjectResult(new { status = 403, message = "There is no group with this id." }) { StatusCode = 403 };

				if (existGroup.IdAdminUser != adminUserId)
					return new ObjectResult(new { status = 403, message = "This group is managed by another user. You are not the administrator." }) { StatusCode = 403 };

				await _context.Database.ExecuteSqlRawAsync(string.Format("INSERT INTO RelationshipGroups VALUES ({0}, {1})", userId, groupId));

				return new ObjectResult(new { status = 200, message = "The memeber was added." }) { StatusCode = 200 };
			}
			catch (DbUpdateConcurrencyException ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}

		// DELETE: api/relationship/group/{groupId}/{userId}
		// Remove a member from group.
		[HttpDelete("group/{groupId:int}/{userId:int}")]
		public async Task<ActionResult<UserModel>> RemoveMemeberFromGroup([FromRoute] int groupId, [FromRoute] int userId)
		{
			try
			{
				var adminUserId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

				var existGroup = await _context.Groups!.FirstOrDefaultAsync(x => x.Id == groupId);

				if (existGroup == null)
					return new ObjectResult(new { status = 403, message = "There is no group with this id." }) { StatusCode = 403 };

				if (existGroup.IdAdminUser != adminUserId)
					return new ObjectResult(new { status = 403, message = "This group is managed by another user. You are not the administrator." }) { StatusCode = 403 };

				await _context.Database.ExecuteSqlRawAsync(string.Format("DELETE FROM RelationshipGroups WHERE IdUser = {0} AND IdGroup = {1}", userId, groupId));

				return new ObjectResult(new { status = 200, message = "The memeber was removed." }) { StatusCode = 200 };
			}
			catch (DbUpdateConcurrencyException ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}

		// DELETE: api/relationship/group/{groupId}
		// Delete a group.
		[HttpDelete("group/{groupId:int}")]
		public async Task<ActionResult<UserModel>> DeleteGroup([FromRoute] int groupId)
		{
			try
			{
				var adminUserId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

				var existGroup = await _context.Groups!.FirstOrDefaultAsync(x => x.Id == groupId);

				if (existGroup == null)
					return new ObjectResult(new { status = 403, message = "There is no group with this id." }) { StatusCode = 403 };

				if (existGroup.IdAdminUser != adminUserId)
					return new ObjectResult(new { status = 403, message = "This group is managed by another user. You are not the administrator." }) { StatusCode = 403 };

				await _context.Database.ExecuteSqlRawAsync(string.Format("DELETE FROM RelationshipGroups WHERE IdGroup = {0}", groupId));

				_context.Groups!.Remove(existGroup);
				await _context.SaveChangesAsync();

				return new ObjectResult(new { status = 200, message = "The group was removed." }) { StatusCode = 200 };
			}
			catch (DbUpdateConcurrencyException ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}
	}
}
