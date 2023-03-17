using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations.Schema;

namespace ChatRoom.Models.DB
{
    [Table("RelationshipGroups")]
    [Keyless]
    public class RelationshipGroupModel
    {
        [ForeignKey("FK_RelationshipGroups_IdUser")]
        [Column("IdUser")]
        public int IdUser { get; set; }

        [ForeignKey("FK_RelationshipGroups_IdGroup")]
        [Column("IdGroup")]
        public int IdGroup { get; set; }
    }
}
