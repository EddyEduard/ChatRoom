using ChatRoom.Data;
using ChatRoom.Models.DB;
using Microsoft.EntityFrameworkCore;
using System.Security.Cryptography;
using System.Text;

namespace ChatRoom.Services
{
    public class AccountManager : IAccountManager
    {
        private readonly ApplicationDbContext _context;

        public AccountManager(ApplicationDbContext context) {
            _context = context;
        }

        /// <summary>
        /// Create user login.
        /// </summary>
        /// <param name="email"></param>
        /// <param name="password"></param>
        /// <returns>
        /// Return a user model if exist an user with get email and password.
        /// </returns>
        public async Task<UserModel?> LoginAsync(string email, string password)
        {
            List<UserModel> _users = await _context.Users!.ToListAsync();
            var user = _users.Find(x => x.Email == email);

            using (MD5 mD5 = MD5.Create())
            {
                mD5.ComputeHash(Encoding.ASCII.GetBytes(password));
                mD5.Initialize();

                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < mD5.Hash!.Length; i++)
                    stringBuilder.Append(mD5.Hash[i].ToString("X2"));

                if (user!.Password == stringBuilder.ToString())
                    return user;
            }

            return null;
        }

        /// <summary>
        /// Create a new register.
        /// </summary>
        /// <param name="userModel"></param>
        /// <param name="password"></param>
        public async Task RegisterAsync(UserModel userModel, string password)
        {
            using (MD5 mD5 = MD5.Create())
            {
                mD5.ComputeHash(Encoding.ASCII.GetBytes(password));
                mD5.Initialize();

                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < mD5.Hash!.Length; i++)
                    stringBuilder.Append(mD5.Hash[i].ToString("X2"));

                userModel.Password = stringBuilder.ToString();
            }

            await _context.Users!.AddAsync(userModel);
            await _context.SaveChangesAsync();
        }

        /// <summary>
        /// Update password user.
        /// </summary>
        /// <param name="userModel"></param>
        /// <param name="newPassword"></param>
        public async Task UpdatePasswordAsync(UserModel userModel, string newPassword)
        {
            using (MD5 mD5 = MD5.Create())
            {
                mD5.ComputeHash(Encoding.ASCII.GetBytes(newPassword));
                mD5.Initialize();

                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < mD5.Hash!.Length; i++)
                    stringBuilder.Append(mD5.Hash[i].ToString("X2"));

                userModel.Password = stringBuilder.ToString();
            }

            _context.Entry(userModel).State = EntityState.Modified;
            await _context.SaveChangesAsync();
        }

        /// <summary>
        /// Check if exist a user with a certain id.
        /// </summary>
        /// <param name="userId"></param>
        /// <returns>
        /// Return 'true' if exist or 'false' if no exist.
        /// </returns>
        public async Task<bool> CheckUserByIdAsync(int userId)
        {
            var user = await _context.Users!.FirstOrDefaultAsync(x => x.Id == userId);

            if (user != null)
                return true;

            return false;
        }

        /// <summary>
        /// Check if exist a user with a certain email.
        /// </summary>
        /// <param name="email"></param>
        /// <returns>
        /// Return 'true' if exist or 'false' if no exist.
        /// </returns>
        public async Task<bool> CheckUserEmailAsync(string email)
        {
            var user = await _context.Users!.FirstOrDefaultAsync(x => x.Email == email);
            
            if (user != null)
                return true;

            return false;
        }

        /// <summary>
        /// Check user password.
        /// </summary>
        /// <param name="email"></param>
        /// <param name="password"></param>
        /// <returns>
        /// Return 'true' if is correct 'false' if not.
        /// </returns>
        public async Task<bool> CheckUserPasswordAsync(string email, string password)
        {
            var user = await _context.Users!.FirstOrDefaultAsync(x => x.Email == email);

            if (user != null)
            {
                using (MD5 mD5 = MD5.Create())
                {
                    mD5.ComputeHash(Encoding.ASCII.GetBytes(password));
                    mD5.Initialize();

                    StringBuilder stringBuilder = new StringBuilder();

                    for (int i = 0; i < mD5.Hash!.Length; i++)
                        stringBuilder.Append(mD5.Hash[i].ToString("X2"));

                    if (user!.Password == stringBuilder.ToString())
                        return true;
                }
            }

            return false;
        }

        /// <summary>
        /// Get user id.
        /// </summary>
        /// <param name="httpContext"></param>
        /// <returns>
        /// Return a value type of int which contains user id.
        /// </returns>
        public int GetUserId(HttpContext httpContext)
        {
            int id = 0;
            int.TryParse(httpContext.User.Claims.First(X => X.Type == "Id").Value, out id);

            return id;
        }

        /// <summary>
        /// Get user by id.
        /// </summary>
        /// <param name="userId"></param>
        /// <returns>
        /// Return a user model if in database is found a user with same id.
        /// </returns>
        public async Task<UserModel> GetUserByIdAsync(int userId)
        {
            List<UserModel> _users = await _context.Users!.ToListAsync();
            var user = _users.Find(x => x.Id == userId);

            return user!;
        }
    }
}
