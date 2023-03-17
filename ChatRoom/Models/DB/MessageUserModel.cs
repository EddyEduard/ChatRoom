using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace ChatRoom.Models.DB
{
    public enum MessageStatus {
        SENT,
        RECEIVED,
        SEEN
    }

    [Table("MessageUsers")]
    public class MessageUserModel
    {
        [Key]
        [Column("Id")]
        public int Id { get; set; }

        [ForeignKey("FK_MessageUsers_IdUserFrom")]
        [Column("IdUserFrom")]
        public int IdUserFrom { get; set; }

        [ForeignKey("FK_MessageUsers_IdUserTo")]
        [Column("IdUserTo")]
        public int IdUserTo { get; set; }

        [Column("Content")]
        [DataType(DataType.Text)]
        public string? Content { get; set; }

        [Column("Status")]
        public MessageStatus Status { get; set; }

        [Column("DateTime")]
        [DataType(DataType.DateTime)]
        public DateTime DateTime { get; set; }
    }
}
