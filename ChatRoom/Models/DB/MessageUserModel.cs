using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace ChatRoom.Models.DB
{
    public enum MessageStatus: int {
        SENT = 0,
        SEEN = 1
    }

    [Table("MessageUsers")]
    public class MessageUserModel
    {
        [Key]
        [Column("Id")]
        public int Id { get; set; }

        [ForeignKey("FK_MessageUsers_IdUserFrom")]
        [Column("IdUserFrom")]
        [JsonPropertyName("id_user_from")]
        public int IdUserFrom { get; set; }

        [ForeignKey("FK_MessageUsers_IdUserTo")]
        [Column("IdUserTo")]
		[JsonPropertyName("id_user_to")]
		public int IdUserTo { get; set; }

        [Column("Content")]
        [DataType(DataType.Text)]
        public string? Content { get; set; }

        [Column("Status")]
        public MessageStatus Status { get; set; }

        [Column("DateTime")]
        [DataType(DataType.DateTime)]
		[JsonPropertyName("date_time")]
		public DateTime DateTime { get; set; }
    }
}
