using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace ChatRoom.Models.DB
{
    [Table("MessageGroups")]
    public class MessageGroupModel
    {
        [Key]
        [Column("Id")]
        public int Id { get; set; }

        [ForeignKey("FK_MessageGroups_IdUser")]
        [Column("IdUser")]
        [JsonPropertyName("id_user")]
        public int IdUser { get; set; }

        [ForeignKey("FK_MessageGroups_IdGroup")]
        [Column("IdGroup")]
        [JsonPropertyName("id_group")]
        public int IdGroup { get; set; }

        [Column("Content")]
        [DataType(DataType.Text)]
        public string? Content { get; set; }

        [Column("Status")]
        public MessageStatus Status { get; set; }

		[Column("SeenMembers")]
		[JsonPropertyName("seen_members")]
		public string? SeenMembers { get; set; }

		[Column("DateTime")]
        [DataType(DataType.DateTime)]
        [JsonPropertyName("date_time")]
        public DateTime DateTime { get; set; }
    }
}
