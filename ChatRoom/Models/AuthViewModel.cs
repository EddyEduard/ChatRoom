using System.ComponentModel.DataAnnotations;

namespace ChatRoom.Models
{
    public class LoginViewModel
    {
        [Required]
        [DataType(DataType.EmailAddress)]
        public string? Email { get; set; }

        [Required]
        [DataType(DataType.Password)]
        [StringLength(30, MinimumLength = 3, ErrorMessage = "Please enter a password with a length between 6-30 characters.")]
        public string? Password { get; set; }
    }

    public class RegisterViewModel
    {
        [Required]
        [StringLength(30, MinimumLength = 3, ErrorMessage = "Please enter a name with a length between 3-30 characters.")]
        public string? Name { get; set; }

        [Required]
        [DataType(DataType.EmailAddress)]
        public string? Email { get; set; }

        [Required]
        [DataType(DataType.Password)]
        [StringLength(30, MinimumLength = 3, ErrorMessage = "Please enter a password with a length between 6-30 characters.")]
        public string? Password { get; set; }
    }
}
