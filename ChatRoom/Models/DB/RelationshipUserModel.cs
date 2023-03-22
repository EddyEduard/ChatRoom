using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations.Schema;

namespace ChatRoom.Models.DB
{
    [Table("RelationshipUsers")]
    [Keyless]
    public class RelationshipUserModel
    {
        [ForeignKey("FK_RelationshipUsers_IdUserFirst")]
        [Column("IdUserFirst")]
        public int IdUserFirst { get; set; }

        [ForeignKey("FK_RelationshipUsers_IdUserSecond")]
        [Column("IdUserSecond")]
        public int IdUserSecond { get; set; }
    }
}
