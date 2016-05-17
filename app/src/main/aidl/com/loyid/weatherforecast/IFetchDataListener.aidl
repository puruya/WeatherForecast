// IFetchDataListener.aidl
package com.loyid.weatherforecast;

// Declare any non-default types here with import statements

interface IFetchDataListener {
    void onWeatherDataRetrieved(out String[] data);
}
