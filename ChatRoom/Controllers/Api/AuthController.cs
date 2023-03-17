using ChatRoom.Models.DB;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;
using System.Security.Claims;
using ChatRoom.Models;
using ChatRoom.Services;

namespace ChatRoom.Controllers.Api
{
    [Route("api/auth")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        private IConfiguration Configuration;

        private ITokenManager _tokenManager;

        private IAccountManager _accountManager;

        public AuthController(IConfiguration configuration, ITokenManager tokenManager, IAccountManager accountManager)
        {
            Configuration = configuration;
            _tokenManager = tokenManager;
            _accountManager = accountManager;
        }

        // POST: Create a new login user.
        [HttpPost("login")]
        public async Task<IActionResult> LoginAsync([FromBody] LoginViewModel loginViewModel)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            try
            {
                var exsitUserWithEmail = await _accountManager.CheckUserEmailAsync(loginViewModel.Email!);

                if (!exsitUserWithEmail)
                    return new ObjectResult(new { status = 403, message = "The email address is incorect." }) { StatusCode = 403 };

                var validUserWithPassword = await _accountManager.CheckUserPasswordAsync(loginViewModel.Email!, loginViewModel.Password!);

                if (!validUserWithPassword)
                    return new ObjectResult(new { status = 403, message = "The password is incorect." }) { StatusCode = 403 };

                var user = await _accountManager.LoginAsync(loginViewModel.Email!, loginViewModel.Password!);

                if(user == null)
                    return new ObjectResult(new { status = 403, message = "Failed login." }) { StatusCode = 403 };

                var claims = new List<Claim>
                {
                    new Claim("Id", user.Id.ToString()),
                    new Claim(ClaimTypes.UserData, JsonConvert.SerializeObject(user))
                };

                HttpContext.User.AddIdentity(new ClaimsIdentity(claims));
                var newToken = _tokenManager.GenerateToken(claims, Configuration.GetValue<string>("SecretKey")!);

                return Ok(new
                {
                    token = newToken,
                    user_id = user.Id,
                    name = user.Name,
                    created_date = DateTime.UtcNow,
                    expire_date = DateTime.UtcNow.AddDays(7)
                });
            }
            catch (Exception ex)
            {
                return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
            }
        }

        // POST: Create a new user account.
        [HttpPost("register")]
        public async Task<IActionResult> RegisterAsync([FromBody] RegisterViewModel registerViewModel)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            try
            {
                var exsitUserWithEmail = await _accountManager.CheckUserEmailAsync(registerViewModel.Email!);

                if (exsitUserWithEmail)
                    return new ObjectResult(new { status = 403, message = "There is a user with this email." }) { StatusCode = 403 };

                await _accountManager.RegisterAsync(new UserModel()
                {
                    Name = registerViewModel.Name,
                    Email = registerViewModel.Email,
                    Password = registerViewModel.Password
                }, registerViewModel.Password!);

                return Ok();
            }
            catch (Exception ex)
            {
                return new ObjectResult(new { status = 500, message = ex.Message }) { StatusCode = 500 };
            }
        }
    }
}
