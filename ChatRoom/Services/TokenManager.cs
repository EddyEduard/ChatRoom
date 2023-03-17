using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace ChatRoom.Services
{
    public class TokenManager : ITokenManager
    {
        /// <summary>
        /// Generate token manager.
        /// </summary>
        /// <param name="claims"></param>
        /// <param name="secureKey"></param>
        /// <returns>
        /// Return a crypto string which contains all data about user logged.
        /// </returns>
        public string GenerateToken(IEnumerable<Claim> claims, string secureKey)
        {
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secureKey));

            var jwt = new JwtSecurityToken(
                issuer: "issuer",
                audience: "audience",
                claims: claims,
                notBefore: DateTime.UtcNow,
                expires: DateTime.UtcNow.AddDays(7),
                signingCredentials: new SigningCredentials(key, SecurityAlgorithms.HmacSha256)
            );

            return new JwtSecurityTokenHandler().WriteToken(jwt);
        }

        /// <summary>
        /// Validate token.
        /// </summary>
        /// <param name="token"></param>
        /// <returns>
        /// Return 'true' if token is valid, otherwise 'false'.
        /// </returns>
        public bool ValidateToken(string token, string secureKey)
        {
            if (token == null)
                return false;

            try
            {
                var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secureKey));

                new JwtSecurityTokenHandler().ValidateToken(token, new TokenValidationParameters
                {
                    ValidateIssuerSigningKey = true,
                    IssuerSigningKey = key,
                    ValidateIssuer = false,
                    ValidateAudience = false,
                    ClockSkew = TimeSpan.Zero
                }, out SecurityToken validatedToken);

                return validatedToken != null;
            } catch
            {
                return false;
            }
        }
    }
}
