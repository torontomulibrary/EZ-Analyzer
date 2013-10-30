EZ-Analyzer
===========

This tool is designed to take a raw EZProxy log, and analyze the data  per User/IP to help determine if a user is abusing your subscriptions.

<strong>Note: The program requires the use of the Maxmind GeoLite City data. It can be downloaded for free here: <a href="http://dev.maxmind.com/geoip/legacy/geolite/">http://dev.maxmind.com/geoip/legacy/geolite/</a>. This file must be placed in the same directory as your compiled source</strong>

In your EZProxy config.txt file, this program requires you to have the following lines:

```
Option LogUser
LogFormat %h %{ezproxy-session}i %l %u %t "%r" %s %b
```

Once the program is compiled and run, you should get a window which allows you to select your log file (you can only select one at a time). It may take a few seconds to process, and then prompt you where to save the file. This will produce 2 CSV files, one for the "Users" activity, and one for the "IP" activity



