DnsLogin
========

Secure authentication for offline-mode Bukkit servers

When the server is in online-mode, it functions like normal. DnsLogin only enables the verification module when the server is in offline mode. Users can only log in if they connect to the correct address. When online the user can generate an authentication key, which is added to the start of the address they connect to. Using DNS-wildcard, these still point to your server.

**Note DNS traffic is not encrypted** unless using services like OpenDNS DnsCrypt. It shouldn't matter much unless you are specifically targeted and on an insecure connection (like open WiFi).

# Example:
Assuming a fictional server play.example.org. Server is in online mode:

1. User connects like normal
2. User executes `/dnslogin auth self`. An authentication key `tjptbhfoycdhcuy0` is generated.
3. DnsLogin tells the user to connect to `tjptbhfoycdhcuy0.play.example.org`

Server is in offline mode:

1. User connects to `tjptbhfoycdhcuy0.play.example.org`
2. Plugin verifies that a user with this name has key `tjptbhfoycdhcuy0`
3. User gets access like normal.

In case in step 2. the key does not match the address the user connected with, the user is presented with *Player not whitelisted.* and disconnected.
