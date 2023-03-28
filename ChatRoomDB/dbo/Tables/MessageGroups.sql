﻿CREATE TABLE [dbo].[MessageGroups]
(
	[Id] INT NOT NULL CONSTRAINT PK_MessageGroup PRIMARY KEY IDENTITY,
	[IdUser] INT NOT NULL CONSTRAINT FK_MessageGroups_User FOREIGN KEY REFERENCES Users(Id),
	[IdGroup] INT NOT NULL CONSTRAINT FK_MessageGroups_Group FOREIGN KEY REFERENCES Groups(Id),
	[Content] TEXT NOT NULL,
	[Status] INT NOT NULL,
	[SeenMembers] VARCHAR(100) NOT NULL,
	[DateTime] DATETIME NOT NULL
)
