CREATE TABLE [dbo].[RelationshipGroups]
(
	[IdUser] INT NOT NULL CONSTRAINT FK_RelationshipGroups_User FOREIGN KEY REFERENCES Users(Id),
	[IdGroup] INT NOT NULL CONSTRAINT FK_RelationshipGroups_Group FOREIGN KEY REFERENCES Groups(Id)
)
