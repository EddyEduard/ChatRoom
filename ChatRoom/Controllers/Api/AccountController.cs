using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ChatRoom.Data;
using ChatRoom.Models.DB;
using Microsoft.AspNetCore.Authorization;
using System.Security.Claims;
using System.Text.Json;
using CloudinaryDotNet.Actions;
using CloudinaryDotNet;

namespace ChatRoom.Controllers.Api
{
    [Authorize]
    [Route("api/account")]
    [ApiController]
    public class AccountController : ControllerBase
    {
		private IConfiguration Configuration;

		private readonly ApplicationDbContext _context;

        private readonly Cloudinary _cloudinary;

        public AccountController(IConfiguration configuration, ApplicationDbContext context)
        {
            Configuration = configuration;

            _context = context;

			_cloudinary = new Cloudinary(new Account(Configuration.GetSection("Cloudinary").GetValue<string>("Cloud")!, Configuration.GetSection("Cloudinary").GetValue<string>("ApiKey")!, Configuration.GetSection("Cloudinary").GetValue<string>("ApiSecret")!));
		}

		// GET: api/account
		// Provide user profile.
		[HttpGet]
        public async Task<ActionResult<UserModel>> GetProfile()
        {
            var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

            var user = await _context.Users!.FirstAsync(x => x.Id == userId);

			IEnumerable<GroupModel> groups = _context.Groups!.Where(g => g.IdAdminUser == userId);

			var resource = await _cloudinary.GetResourceAsync("chatroom/users/" + userId.ToString());

            if (resource.SecureUrl != null)
                user.Image = resource.SecureUrl;

            foreach (var group in groups)
            {
                resource = await _cloudinary.GetResourceAsync("chatroom/groups/" + group!.Id.ToString());

                if (resource.SecureUrl != null)
                    group.Image = resource.SecureUrl;
            }

            return new ObjectResult(new { user, groups }) { StatusCode = 200 };
		}
        
        // PUT: api/account
        // Update profile.
        [HttpPut]
        public async Task<IActionResult> UpdateProfile([FromBody] JsonElement body)
        {
            try
            {
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());
				string name = body.GetProperty("name").ToString();
				string email = body.GetProperty("email").ToString();

				var user = await _context.Users!.FirstAsync(x => x.Id == userId);

                if(user != null)
                {
                    user.Name = name;
                    user.Email = email;
                }

				_context.Entry(user!).State = EntityState.Modified;
                await _context.SaveChangesAsync();

                return new ObjectResult(new { status = 200, message = "The profile has been changed successfully." }) { StatusCode = 200 };
			}
            catch (Exception ex)
            {
                return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
            }
        }

		// PUT: api/account/profile-image
		// Update profile image.
		[HttpPut("profile-image")]
		public async Task<IActionResult> UpdateProfileImage(IFormFile image)
		{
			try
			{
				var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

                using (var stream = new MemoryStream())
				{
					await image.CopyToAsync(stream);
					stream.Seek(0, SeekOrigin.Begin);

					var uploadParams = new ImageUploadParams()
					{
						File = new FileDescription(image.FileName, stream),
						PublicId = userId.ToString(),
						Folder = "chatroom/users",
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

		// DELETE: api/account
		// Delete account.
		[HttpDelete]
        public async Task<IActionResult> DeleteUser()
        {
            try
            {
                var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());
                var user = await _context.Users!.FindAsync(userId);

                if (user == null)
                    return new ObjectResult(new { status = 403, message = "User not found." }) { StatusCode = 403 };

				IEnumerable<GroupModel> groups = _context.Groups!.Where(g => g.IdAdminUser == userId);

                if (groups.Count() > 0)
                {
                    foreach (var group in groups)
                        await _cloudinary.DeleteResourcesAsync(new string[] { "chatroom/groups/" + group!.Id.ToString() });
                }

				await _context.Database.ExecuteSqlRawAsync(string.Format("DELETE FROM RelationshipUsers WHERE IdUserFirst = {0} OR IdUserSecond = {0}", userId));
				await _context.Database.ExecuteSqlRawAsync(string.Format("DELETE FROM RelationshipGroups WHERE IdUser = {0}", userId));
				await _context.Database.ExecuteSqlRawAsync(string.Format("DELETE FROM MessageUsers WHERE IdUserFrom = {0} OR IdUserTo = {0}", userId));
				await _context.Database.ExecuteSqlRawAsync(string.Format("DELETE FROM MessageGroups WHERE IdUser = {0}", userId));
				await _context.Database.ExecuteSqlRawAsync(string.Format("DELETE FROM Groups WHERE IdAdminUser = {0}", userId));
				await _context.Database.ExecuteSqlRawAsync(string.Format("DELETE FROM Users WHERE Id = {0}", userId));

                await _cloudinary.DeleteResourcesAsync(new string[] { "chatroom/users/" + userId.ToString() });

				return new ObjectResult(new { status = 200, message = "The account has been deleted." }) { StatusCode = 200 };
            }
            catch (DbUpdateConcurrencyException ex)
            {
                return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
            }
        }
    }
}
