using ChatRoom.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace ChatRoom.Middleware
{
    [AttributeUsage(AttributeTargets.Class)]
    public class AuthorizeToken : Attribute, IAsyncAuthorizationFilter
    {
        private IConfiguration? Configuration;

        private ITokenManager? tokenManager;

        public Task OnAuthorizationAsync(AuthorizationFilterContext context)
        {
            Configuration = context.HttpContext.RequestServices.GetRequiredService<IConfiguration>();
            tokenManager = context.HttpContext.RequestServices.GetService<ITokenManager>();

            string? token = null;
            IEnumerator<KeyValuePair<string, string>> cookies = context.HttpContext.Request.Cookies.GetEnumerator();

            while (cookies.MoveNext())
            {
                var key = cookies.Current.Key;
                var value = cookies.Current.Value;

                if (key == "token")
                {
                    token = value;
                    break;
                }
            }

            if (token == null || token == "")
            {
                context.Result = new RedirectResult(url: "/auth/login", permanent: true, preserveMethod: true);
            }
            else
            {
                if (!tokenManager!.ValidateToken(token, Configuration?.GetValue<string>("SecretKey")!))
                    context.Result = new RedirectResult(url: "/auth/login", permanent: true, preserveMethod: true);
            }

            return Task.CompletedTask;
        }
    }
}
