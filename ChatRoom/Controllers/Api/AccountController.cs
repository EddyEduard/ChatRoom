using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ChatRoom.Data;
using ChatRoom.Models.DB;
using Microsoft.AspNetCore.Authorization;
using System.Security.Claims;

namespace ChatRoom.Controllers.Api
{
    [Authorize]
    [Route("api/account")]
    [ApiController]
    public class AccountController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public AccountController(ApplicationDbContext context)
        {
            _context = context;
        }

        // GET: api/account
        // Provide user profile.
        [HttpGet]
        public async Task<ActionResult<UserModel>> GetProfile()
        {
            var userId = int.Parse(HttpContext.User.FindFirstValue("Id")!.ToString());

            return await _context.Users!.FirstAsync(x => x.Id == userId);
        }
        
        // PUT: api/account
        // Update user profile.
        [HttpPut]
        public async Task<IActionResult> UpdateProfile([FromBody] UserModel userModel)
        {
            try
            {
                _context.Entry(userModel).State = EntityState.Modified;
                await _context.SaveChangesAsync();

                return Ok();
            }
            catch (DbUpdateConcurrencyException ex)
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

                _context.Users.Remove(user);
                await _context.SaveChangesAsync();

                return new ObjectResult(new { status = 200, message = "The account has been deleted." }) { StatusCode = 200 };
            }
            catch (DbUpdateConcurrencyException ex)
            {
                return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
            }
        }
    }
}
