using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace ChatRoom.Models.DB
{
    [Table("Groups")]
    public class GroupModel
    {
        [Key]
        [Column("Id")]
        public int Id { get; set; }

        [Column("Name")]
        public string? Name { get; set; }

        [ForeignKey("FK_Groups_AdminUser")]
        [Column("IdAdminUser")]
        public int IdAdminUser { get; set; }

		[NotMapped]
		[JsonPropertyName("status")]
		public string Status { get; set; } = "OFFLINE";

		[NotMapped]
		[JsonPropertyName("last_message")]
		public MessageGroupModel? LastMessage { get; set; } = new MessageGroupModel();
	}
}
