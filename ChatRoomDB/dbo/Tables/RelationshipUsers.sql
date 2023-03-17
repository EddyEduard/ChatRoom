CREATE TABLE [dbo].[RelationshipUsers]
(
	[IdUserFirst] INT NOT NULL CONSTRAINT FK_RelationshipUsers_UserFirst FOREIGN KEY REFERENCES Users(Id),
	[IdUserSecond] INT NOT NULL CONSTRAINT FK_RelationshipUsers_UserSecond FOREIGN KEY REFERENCES Users(Id)
)
