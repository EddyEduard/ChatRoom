using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

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
        [Column("IsAdminUser")]
        public int IdAdminUser { get; set; }
    }
}
