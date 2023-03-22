using Microsoft.AspNetCore.SignalR;

namespace ChatRoom.Services.Hubs
{
    public class ChatHub : Hub
    {
        private static Dictionary<int, string> _onlineUsers = new Dictionary<int, string>();

		private static Dictionary<int, List<string>> _onlineGroups = new Dictionary<int, List<string>>();

		// Connecting to socket.
		public override Task OnConnectedAsync()
        {
			return Task.CompletedTask;
        }

        // Disconnecting from socket.
		public override async Task OnDisconnectedAsync(Exception? exception)
        {
            _onlineUsers.Remove(_onlineUsers.First(x => x.Value == Context.ConnectionId).Key);

			var onlineGroupConnectionIDs = _onlineGroups.FirstOrDefault(x => x.Value.Contains(Context.ConnectionId));
			
			if(_onlineGroups.Any(x => x.Value.Contains(Context.ConnectionId)))
				onlineGroupConnectionIDs.Value.Remove(Context.ConnectionId);

			await Clients.All.SendAsync("OnlineUsers", _onlineUsers.Keys);
			await Clients.All.SendAsync("OnlineGroups", _onlineGroups.Keys);
		}

		// Connect user to chat.
		public async Task ConnectUser(int userId)
		{
			if (!_onlineUsers.ContainsKey(userId))
				_onlineUsers.Add(userId, Context.ConnectionId);

			await Clients.All.SendAsync("OnlineUsers", _onlineUsers.Keys);
		}

		// Connect group to chat.
		public async Task ConnectGroup(int groupId)
		{
			if (!_onlineGroups.ContainsKey(groupId))
				_onlineGroups.Add(groupId, new List<string> { Context.ConnectionId });
			else
				_onlineGroups[groupId].Add(Context.ConnectionId);

			await Clients.All.SendAsync("OnlineGroups", _onlineGroups.Keys);
		}

		// Provide connected users and groups.
		public async Task ProvideConnectedUsersAndGroups()
		{
			await Clients.All.SendAsync("OnlineUsers", _onlineUsers.Keys);
			await Clients.All.SendAsync("OnlineGroups", _onlineGroups.Keys);
		}

		// Send message to a user.
		public async Task SendMessageToUser(int fromUserId, int toUserId, string message, string dateTime)
        {
			if (_onlineUsers.ContainsKey(toUserId))
			{
                var connectionId = _onlineUsers[toUserId];

				await Clients.Client(connectionId).SendAsync("ReceiveMessageFromUser", fromUserId, message, dateTime);
			}
		}

		// Send message to a group.
		public async Task SendMessageToGroup(int userId, int groupId, string message, string dateTime)
		{
			if (_onlineGroups.ContainsKey(groupId))
			{
				var connectionIds = _onlineGroups[groupId];

				foreach (var connectionId in connectionIds)
				{
					Console.WriteLine(connectionId);
					await Clients.Client(connectionId).SendAsync("ReceiveMessageFromGroup", userId, groupId, message, dateTime);
				}
			}
		}
	}
}