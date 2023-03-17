using System.Security.Claims;

namespace ChatRoom.Services
{
    public interface ITokenManager
    {
        string GenerateToken(IEnumerable<Claim> claims, string secureKey);
        bool ValidateToken(string token, string secureKey);
    }
}
