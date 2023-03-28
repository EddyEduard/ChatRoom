using ChatRoom.Data;
using ChatRoom.Models.DB;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using System.Text.Json;

namespace ChatRoom.Controllers.Api
{
	[Authorize]
	[Route("api/message")]
	[ApiController]
	public class MessageController : ControllerBase
	{
		private readonly ApplicationDbContext _context;

		public MessageController(ApplicationDbContext context)
		{
			_context = context;
		}

		// GET: api/message/{secondUserId}
		// Provide all messages from a relationship between two users.
		[HttpGet("{secondUserId:int}")]
		public async Task<ActionResult<IEnumerable<MessageUserModel>>> GetMessagesFromUsers([FromRoute] int secondUserId)
		{
			try
			{
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

				var relationshipUsers = await _context.RelationshipUsers!.FirstOrDefaultAsync(x => (x.IdUserFirst == userId && x.IdUserSecond == secondUserId) || (x.IdUserFirst == secondUserId && x.IdUserSecond == userId));

				if (relationshipUsers == null)
					return new ObjectResult(new { status = 403, message = "There is no relationship between these two users." }) { StatusCode = 403 };

				IEnumerable<MessageUserModel> messages = _context.MessageUsers!.Where(x => (x.IdUserFrom == userId && x.IdUserTo == secondUserId) || (x.IdUserFrom == secondUserId && x.IdUserTo == userId));

				return Ok(messages);
			}
			catch (DbUpdateConcurrencyException ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}

		// GET: api/message/group/groupId}
		// Provide all messages from a group.
		[HttpGet("group/{groupId:int}")]
		public async Task<ActionResult<IEnumerable<MessageGroupModel>>> GetMessagesFromGroup([FromRoute] int groupId)
		{
			try
			{
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

				var group = await _context.Groups!.FirstOrDefaultAsync(x => x.Id == groupId);

				if (group == null)
					return new ObjectResult(new { status = 403, message = "There is no group with this id." }) { StatusCode = 403 };

				var relationshipGroup = await _context.RelationshipGroups!.FirstOrDefaultAsync(x => x.IdGroup == groupId && x.IdUser == userId);

				if(relationshipGroup == null)
					return new ObjectResult(new { status = 403, message = "You are not part of this group. You cannot view the messages." }) { StatusCode = 403 };

				IEnumerable<MessageGroupModel> messages = _context.MessageGroups!.Where(x => x.IdGroup == groupId);

				foreach (var message in messages)
					message.User = await _context.Users!.FirstOrDefaultAsync(x => x.Id == message.IdUser);
				
				return Ok(messages);
			}
			catch (DbUpdateConcurrencyException ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}

		// POST: api/message
		// Send a new message to a user.
		[HttpPost]
		public async Task<ActionResult<MessageUserModel>> SendMessageToUser([FromBody] JsonElement body)
		{
			try
			{
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());
				int secondUserId = body.GetProperty("user_id").GetInt32();
				string message = body.GetProperty("message").ToString();
				
				if (userId == secondUserId)
					return new ObjectResult(new { status = 403, message = "You cannot send yourself a message." }) { StatusCode = 403 };

				var firstUser = await _context.Users!.FirstOrDefaultAsync(x => x.Id == userId);

				if (firstUser == null)
					return new ObjectResult(new { status = 403, message = "There is no user with this first id." }) { StatusCode = 403 };

				var secondUser = await _context.Users!.FirstOrDefaultAsync(x => x.Id == secondUserId);

				if (secondUser == null)
					return new ObjectResult(new { status = 403, message = "There is no user with this second id." }) { StatusCode = 403 };

				var relationshipUsers = await _context.RelationshipUsers!.FirstOrDefaultAsync(x => (x.IdUserFirst == userId && x.IdUserSecond == secondUserId) || (x.IdUserFirst == secondUserId && x.IdUserSecond == userId));

				if (relationshipUsers == null)
					return new ObjectResult(new { status = 403, message = "There is no relationship between these two users." }) { StatusCode = 403 };

				_context.MessageUsers!.Add(new MessageUserModel() 
				{ 
					IdUserFrom = userId, 
					IdUserTo = secondUserId, 
					Content = message, 
					Status = MessageStatus.SENT, 
					DateTime = DateTime.Now 
				});
				await _context.SaveChangesAsync();

				return new ObjectResult(new { status = 200, message = "The message was sent." }) { StatusCode = 200 };
			}
			catch (DbUpdateConcurrencyException ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}

		// PUT: api/message/{secondUserId}
		// Mark as seen all messages from a conversation between two users.
		[HttpPut("{secondUserId:int}")]
		public async Task<ActionResult<MessageUserModel>> MarkAsSeenMessagesFromUsers([FromRoute] int secondUserId)
		{
			try
			{
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

				var firstUser = await _context.Users!.FirstOrDefaultAsync(x => x.Id == userId);

				if (firstUser == null)
					return new ObjectResult(new { status = 403, message = "There is no user with this first id." }) { StatusCode = 403 };

				var secondUser = await _context.Users!.FirstOrDefaultAsync(x => x.Id == secondUserId);

				if (secondUser == null)
					return new ObjectResult(new { status = 403, message = "There is no user with this second id." }) { StatusCode = 403 };

				var relationshipUsers = await _context.RelationshipUsers!.FirstOrDefaultAsync(x => (x.IdUserFirst == userId && x.IdUserSecond == secondUserId) || (x.IdUserFirst == secondUserId && x.IdUserSecond == userId));

				if (relationshipUsers == null)
					return new ObjectResult(new { status = 403, message = "There is no relationship between these two users." }) { StatusCode = 403 };

				IEnumerable<MessageUserModel> sentMessages = _context.MessageUsers!.Where(x => x.Status == MessageStatus.SENT).ToList();

				foreach (var messageUser in sentMessages)
				{
					messageUser.Status = MessageStatus.SEEN;
					_context.Entry(messageUser).State = EntityState.Modified;
				}

				await _context.SaveChangesAsync();

				return new ObjectResult(new { status = 200, message = "The messages was marked as seen." }) { StatusCode = 200 };
			}
			catch (DbUpdateConcurrencyException ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}

		// POST: api/message/group
		// Send a new message to group.
		[HttpPost("group")]
		public async Task<ActionResult<MessageUserModel>> SendMessageToGroup([FromBody] JsonElement body)
		{
			try
			{
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());
				int groupId = body.GetProperty("group_id").GetInt32();
				string message = body.GetProperty("message").ToString();

				var firstUser = await _context.Users!.FirstOrDefaultAsync(x => x.Id == userId);

				if (firstUser == null)
					return new ObjectResult(new { status = 403, message = "There is no user with this first id." }) { StatusCode = 403 };

				var group = await _context.Groups!.FirstOrDefaultAsync(x => x.Id == groupId);

				if (group == null)
					return new ObjectResult(new { status = 403, message = "There is no group with this id." }) { StatusCode = 403 };

				var relationshipGroup = await _context.RelationshipGroups!.FirstOrDefaultAsync(x => x.IdGroup == groupId && x.IdUser == userId);

				if (relationshipGroup == null)
					return new ObjectResult(new { status = 403, message = "You are not part of this group. You cannot send the message." }) { StatusCode = 403 };

				_context.MessageGroups!.Add(new MessageGroupModel()
				{
					IdGroup = groupId,
					IdUser = userId,
					Content = message,
					Status = MessageStatus.SENT,
					SeenMembers = userId.ToString(),
					DateTime = DateTime.Now
				});
				await _context.SaveChangesAsync();

				return new ObjectResult(new { status = 200, message = "The message was sent." }) { StatusCode = 200 };
			}
			catch (DbUpdateConcurrencyException ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}

		// PUT: api/message/group/{groupId}
		// Mark as seen all messages from a group for specific member.
		[HttpPut("group/{groupId:int}")]
		public async Task<ActionResult<MessageUserModel>> MarkAsSeenMessagesFromGroup([FromRoute] int groupId)
		{
			try
			{
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

				var firstUser = await _context.Users!.FirstOrDefaultAsync(x => x.Id == userId);

				if (firstUser == null)
					return new ObjectResult(new { status = 403, message = "There is no user with this first id." }) { StatusCode = 403 };

				var group = await _context.Groups!.FirstOrDefaultAsync(x => x.Id == groupId);

				if (group == null)
					return new ObjectResult(new { status = 403, message = "There is no group with this id." }) { StatusCode = 403 };

				var relationshipGroup = await _context.RelationshipGroups!.FirstOrDefaultAsync(x => x.IdGroup == groupId && x.IdUser == userId);

				if (relationshipGroup == null)
					return new ObjectResult(new { status = 403, message = "You are not part of this group. You cannot send the message." }) { StatusCode = 403 };

				IEnumerable<MessageGroupModel> sentMessages = _context.MessageGroups!.Where(x => x.Status == MessageStatus.SENT).ToList();
				IEnumerable<RelationshipGroupModel> groupMembers = _context.RelationshipGroups!.Where(x => x.IdGroup == groupId).ToList();
				
				foreach (var messageGroup in sentMessages)
				{
					if (messageGroup.SeenMembers?.IndexOf(userId.ToString()) == -1)
					{
						messageGroup.SeenMembers += "," + userId;
						_context.Entry(messageGroup).State = EntityState.Modified;
					}

                    if (groupMembers.Count() == messageGroup.SeenMembers?.ToString()?.Split(",").Length)
                        messageGroup.Status = MessageStatus.SEEN;
                }

				await _context.SaveChangesAsync();

				return new ObjectResult(new { status = 200, message = "The messages was marked as seen." }) { StatusCode = 200 };
			}
			catch (DbUpdateConcurrencyException ex)
			{
				return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
			}
		}
	}
}
