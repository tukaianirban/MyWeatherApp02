# MyWeatherApp02

MyWeatherApp is an Android app to fetch and display weather information from OpenWeatherApi. This version of the app assumes use of a "free" key from OpenWeatherApi which provides only:
1. weather : current weather information of a location
2. forecast : 3hr interval forecast information of a given location.

The app's main activity comes along with a ViewPager that flips through the user-selected cities and on each flip shows the current weather and forecast for the given city. On the device home screen, the user can have a widget to get information of a chosen city. The user can choose to have any number of cities, chosen from the 20K cities serviced by the free version of OpenWeatherApi keys.

The list of cities that the user can choose from, is attached as a resource in the app, as a json-formatted list that is available from openweatherapi: http://bulk.openweathermap.org/sample/city.list.json.gz

A few points to note for whoever clones and plans to use this project code in whole or part:
1. The app is made with purely functional needs in mind. No effort has been spent on UI design and UX.
2. You need to use your own key when placing the API query call to openweatherapi servers. For API documentation, refer to: http://www.openweathermap.com/api
3. This project is made with the sole intent of being used for personal usages, weather research and is was not meant to be distributed out for public use. Anyone cloning / pulling this project is wholly responsible for copyrights and ethical usage of the code. Please refer to copyrights and other legal advises + notices from OpenWeatherApi before cloning / pulling / using any part of the code here.
4. This project is under work and does have some open faults in it.
