using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

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
        [JsonIgnore]
        public string? Password { get; set; }

		[NotMapped]
		[JsonPropertyName("image")]
		public string Image { get; set; } = "https://cdn-icons-png.flaticon.com/512/149/149071.png";

		[NotMapped]
		[JsonPropertyName("status")]
		public string Status { get; set; } = "OFFLINE";

		[NotMapped]
		[JsonPropertyName("last_message")]
		public MessageUserModel? LastMessage { get; set; } = new MessageUserModel();
	}
}
