// IFetchWeatherService.aidl
package com.loyid.weatherforecast;

// Declare any non-default types here with import statements
import com.loyid.weatherforecast.IFetchDataListener;

interface IFetchWeatherService {
    void retrieveWeatherData();
    void registerFetchDataListener(IFetchDataListener listener);
    void unregisterFetchDataListener(IFetchDataListener listener);
}
