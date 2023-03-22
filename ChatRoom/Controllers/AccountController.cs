using ChatRoom.Middleware;
using Microsoft.AspNetCore.Mvc;

namespace ChatRoom.Controllers
{
    [AuthorizeToken]
    public class AccountController : Controller
    {
        public IActionResult Chat()
        {
            return View();
        }
    }
}
