using ChatRoom.Models.DB;

namespace ChatRoom.Services
{
    public interface IAccountManager
    {
        Task<UserModel?> LoginAsync(string email, string password);
        Task RegisterAsync(UserModel userModel, string password);
        Task UpdateUserAsync(UserModel userModel);
        Task UpdatePasswordAsync(UserModel userModel, string newPassword);
        int GetUserId(HttpContext httpContext);
        Task<bool> CheckUserEmailAsync(string email);
        Task<bool> CheckUserPasswordAsync(string email, string password);
        Task<bool> CheckUserByIdAsync(int userId);
        Task<UserModel> GetUserByIdAsync(int userId);
    }
}
