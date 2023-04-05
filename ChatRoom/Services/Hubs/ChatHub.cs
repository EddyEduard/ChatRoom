using Microsoft.AspNetCore.SignalR;

namespace ChatRoom.Services.Hubs
{
    public class ChatHub : Hub
    {
        private static Dictionary<int, string> _onlineUsersOnBrowser = new Dictionary<int, string>();

		private static Dictionary<int, string> _onlineUsersOnMobile = new Dictionary<int, string>();

		private static Dictionary<int, List<string>> _onlineGroupsOnBrowser = new Dictionary<int, List<string>>();

		private static Dictionary<int, List<string>> _onlineGroupsOnMobile = new Dictionary<int, List<string>>();

		// Connecting to socket.
		public override Task OnConnectedAsync()
        {
			return Task.CompletedTask;
        }

		// Disconnecting from socket.
		public override async Task OnDisconnectedAsync(Exception? exception)
		{
			bool existConnectionUserOnBrowser = false;
			var userConnectionOnBrowser = _onlineUsersOnBrowser.FirstOrDefault(x => x.Value == Context.ConnectionId && (existConnectionUserOnBrowser = true));
			
			if (existConnectionUserOnBrowser)
				_onlineUsersOnBrowser.Remove(userConnectionOnBrowser.Key);

			bool existConnectionUserOnMobile = false;
			var userConnectionOnMobile = _onlineUsersOnMobile.FirstOrDefault(x => x.Value == Context.ConnectionId && (existConnectionUserOnMobile = true));
			
			if (existConnectionUserOnMobile)
				_onlineUsersOnMobile.Remove(userConnectionOnMobile.Key);

			var onlineGroupOnBrowserConnectionIDs = _onlineGroupsOnBrowser.FirstOrDefault(x => x.Value.Contains(Context.ConnectionId));

			if (_onlineGroupsOnBrowser.Any(x => x.Value.Contains(Context.ConnectionId)))
				onlineGroupOnBrowserConnectionIDs.Value.Remove(Context.ConnectionId);

			var onlineGroupOnMobileConnectionIDs = _onlineGroupsOnMobile.FirstOrDefault(x => x.Value.Contains(Context.ConnectionId));

			if (_onlineGroupsOnMobile.Any(x => x.Value.Contains(Context.ConnectionId)))
				onlineGroupOnMobileConnectionIDs.Value.Remove(Context.ConnectionId);

			await Clients.All.SendAsync("OnlineUsers", _onlineUsersOnBrowser.Keys.Union(_onlineUsersOnMobile.Keys));
			await Clients.All.SendAsync("OnlineGroups", _onlineGroupsOnBrowser.Keys.Union(_onlineGroupsOnMobile.Keys));
		}

		// Connect user to chat.
		public async Task ConnectUser(int userId, string device)
		{
			if (device == "BROWSER")
			{
				if (!_onlineUsersOnBrowser.ContainsKey(userId))
					_onlineUsersOnBrowser.Add(userId, Context.ConnectionId);
				else
				{
					_onlineUsersOnBrowser.Remove(userId);
					_onlineUsersOnBrowser.Add(userId, Context.ConnectionId);
				}
			} else if(device == "MOBILE")
			{
				if (!_onlineUsersOnMobile.ContainsKey(userId))
					_onlineUsersOnMobile.Add(userId, Context.ConnectionId);
				else
				{
					_onlineUsersOnMobile.Remove(userId);
					_onlineUsersOnMobile.Add(userId, Context.ConnectionId);
				}
			}

			await Clients.All.SendAsync("OnlineUsers", _onlineUsersOnBrowser.Keys.Union(_onlineUsersOnMobile.Keys));
		}

		// Connect group to chat.
		public async Task ConnectGroup(int groupId, string device)
		{
			if (device == "BROWSER")
			{
				if (!_onlineGroupsOnBrowser.ContainsKey(groupId))
					_onlineGroupsOnBrowser.Add(groupId, new List<string> { Context.ConnectionId });
				else
					_onlineGroupsOnBrowser[groupId].Add(Context.ConnectionId);
			}
			else if (device == "MOBILE")
			{
				if (!_onlineGroupsOnMobile.ContainsKey(groupId))
					_onlineGroupsOnMobile.Add(groupId, new List<string> { Context.ConnectionId });
				else
					_onlineGroupsOnMobile[groupId].Add(Context.ConnectionId);
			}

			await Clients.All.SendAsync("OnlineGroups", _onlineGroupsOnBrowser.Keys.Union(_onlineGroupsOnMobile.Keys));
		}

		// Provide connected users and groups.
		public async Task ProvideConnectedUsersAndGroups()
		{
			await Clients.All.SendAsync("OnlineUsers", _onlineUsersOnBrowser.Keys.Union(_onlineUsersOnMobile.Keys));
			await Clients.All.SendAsync("OnlineGroups", _onlineGroupsOnBrowser.Keys.Union(_onlineGroupsOnMobile.Keys));
		}

		// Send message to a user.
		public async Task SendMessageToUser(int fromUserId, int toUserId, string message, string dateTime)
        {
			if (_onlineUsersOnBrowser.ContainsKey(toUserId))
			{
                var connectionId = _onlineUsersOnBrowser[toUserId];
				
				await Clients.Client(connectionId).SendAsync("ReceiveMessageFromUser", fromUserId, message, dateTime);
			}

			if (_onlineUsersOnMobile.ContainsKey(toUserId))
			{
				var connectionId = _onlineUsersOnMobile[toUserId];

				await Clients.Client(connectionId).SendAsync("ReceiveMessageFromUser", fromUserId, message, dateTime);
			}
		}

		// Send message to a group.
		public async Task SendMessageToGroup(int userId, int groupId, string message, string dateTime, string name, string image)
		{
			if (_onlineGroupsOnBrowser.ContainsKey(groupId))
			{
				var connectionIds = _onlineGroupsOnBrowser[groupId];
				var previewCoonectionId = "";

				foreach (var connectionId in connectionIds)
				{
					if (connectionId != previewCoonectionId)
					{
						await Clients.Client(connectionId).SendAsync("ReceiveMessageFromGroup", userId, groupId, message, dateTime, name, image);
						
						previewCoonectionId = connectionId;
					}
				}
			}
			
			if (_onlineGroupsOnMobile.ContainsKey(groupId))
			{
				var connectionIds = _onlineGroupsOnMobile[groupId];
				var previewCoonectionId = "";

				foreach (var connectionId in connectionIds)
				{
					if (connectionId != previewCoonectionId)
					{
						await Clients.Client(connectionId).SendAsync("ReceiveMessageFromGroup", userId, groupId, message, dateTime, name, image);

						previewCoonectionId = connectionId;
					}
				}
			}
		}

		// Seen messages from user.
		public async Task SeenMessagesFromUser(int fromUserId, int toUserId)
		{
			if (_onlineUsersOnBrowser.ContainsKey(toUserId))
			{
				var connectionId = _onlineUsersOnBrowser[toUserId];

				await Clients.Client(connectionId).SendAsync("ReceiveSeenMessagesFromUser", fromUserId);
			}

			if (_onlineUsersOnMobile.ContainsKey(toUserId))
			{
				var connectionId = _onlineUsersOnMobile[toUserId];

				await Clients.Client(connectionId).SendAsync("ReceiveSeenMessagesFromUser", fromUserId);
			}
		}
	}
}