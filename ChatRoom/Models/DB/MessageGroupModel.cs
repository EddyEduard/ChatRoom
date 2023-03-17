using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;

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
        public int IdUser { get; set; }

        [ForeignKey("FK_MessageGroups_IdGroup")]
        [Column("IdGroup")]
        public int IdGroup { get; set; }

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
