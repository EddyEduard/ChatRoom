using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace ChatRoom.Models.DB
{
    [Table("Users")]
    public class UserModel
    {
        [Key]
        [Column("Id")]
        public int Id { get; set; }

        [Column("Name")]
        public string? Name { get; set; }

        [Column("Email")]
        [DataType(DataType.EmailAddress)]
        public string? Email { get; set; }

        [Column("Password")]
        [DataType(DataType.Password)]
        public string? Password { get; set; }
    }
}
