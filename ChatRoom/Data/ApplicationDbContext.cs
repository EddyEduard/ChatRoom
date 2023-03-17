using ChatRoom.Models.DB;
using Microsoft.EntityFrameworkCore;

namespace ChatRoom.Data
{
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
            : base(options)
        {
        }

        public DbSet<UserModel>? Users { get; set; }

        public DbSet<GroupModel>? Groups { get; set; }

        public DbSet<RelationshipUserModel>? RelationshipUsers { get; set; }

        public DbSet<RelationshipGroupModel>? RelationshipGroups { get; set; }

        public DbSet<MessageUserModel>? MessageUsers { get; set; }

        public DbSet<MessageGroupModel>? MessageGroups { get; set; }
    }
}