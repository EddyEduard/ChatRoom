﻿CREATE TABLE [dbo].[Users]
(
	[Id] INT NOT NULL CONSTRAINT PK_User PRIMARY KEY IDENTITY,
	[Name] VARCHAR(50) NOT NULL,
	[Email] VARCHAR(50) NOT NULL,
	[Password] VARCHAR(50) NOT NULL
)