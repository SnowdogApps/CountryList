package snowdog.pl.countrylist;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.MissingResourceException;

import snowdog.pl.countrylist.model.Country;


public class CountrListActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public static final String TAG = "CountryListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO testowe todo
        setContentView(R.layout.activity_countr_list);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.countr_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        public static final String TAG = "PlaceholderFragment";
        private Spinner spCountryList;
        private ArrayList<Country> lsCountries;
        private AutoCompleteTextView mAutoCompleteCountries;

        public PlaceholderFragment() {
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ReverseGeocodingTask reverseGeocodingTask = new ReverseGeocodingTask(getActivity(), new ReverseGeocodingTask.ReverseGeocodingListener() {
                @Override
                public void onAddressFound(Address address) {
                    int positionCounter = 0;
                    for (Country country : lsCountries) {
                        if (country.getCode().equals(address.getCountryCode())) {
                            break;
                        }
                        positionCounter++;
                    }
                    spCountryList.setSelection(positionCounter);
                    Log.d(TAG, "onAddressFound address: "+address.toString());
                }

                @Override
                public void onAddressNotFound() {
                    Log.d(TAG, "onAddressNotFound");
                }
            });
            LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            double longitude = 0;
            double latitude = 0;
            if(location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.d(TAG, "location found using NETWORK_PROVIDER");
            } else {
                //Try passive provider
                location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                longitude = location.getLongitude();
                latitude = location.getLatitude();

                Log.d(TAG, "location found using PASSIVE_PROVIDER");
            }
            if(latitude != 0 && longitude != 0) {
                reverseGeocodingTask.execute(latitude, longitude);
            }
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_countr_list, container, false);
            spCountryList = (Spinner) rootView.findViewById(R.id.spinner_countrylist);
            mAutoCompleteCountries = (AutoCompleteTextView) rootView.findViewById(R.id.autocomplete_country);

            lsCountries = new ArrayList<Country>();

            String[] aryCountryNames;
            String[] aryCountryCodes;
            aryCountryNames = getActivity().getResources().getStringArray(R.array.countries);
            aryCountryCodes = getActivity().getResources().getStringArray(R.array.countries_codes);

            String locale = "";
            int countryId = 0;

            //get country code
            try {
                locale = getResources().getConfiguration().locale.getISO3Country();
            } catch (MissingResourceException ex) {
                Log.d(TAG, ex.toString());
            } catch (NullPointerException ex) {
                Log.d(TAG, ex.toString());
            } catch (Exception ex) {
                Log.d(TAG, ex.toString());
            }

            for (int i = 0; aryCountryCodes.length > i; i++) {
                lsCountries.add(new Country(aryCountryCodes[i], aryCountryNames[i]));
                if (locale != null && locale.length() > 0 && aryCountryCodes[i].equalsIgnoreCase(locale)) {
                    countryId = i;
                }
            }

            ArrayAdapter<Country> adapter = new ArrayAdapter<Country>(getActivity(), android.R.layout.simple_spinner_item, lsCountries);
            spCountryList.setAdapter(adapter);
            spCountryList.setSelection(countryId);

            ArrayAdapter<Country> autoCompleteAdapter =
                    new ArrayAdapter<Country>(getActivity(), android.R.layout.simple_list_item_1, lsCountries);
            mAutoCompleteCountries.setAdapter(autoCompleteAdapter);

            mAutoCompleteCountries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Country selectedCountry = (Country) parent.getItemAtPosition(position);
                    int positionCounter = 0;
                    for (Country country : lsCountries) {
                        if (country.equals(selectedCountry)) {
                            break;
                        }
                        positionCounter++;
                    }
//                    spCountryList.setSelection(positionCounter);
                    Log.d(TAG, "onItemClick: " + positionCounter + " country name: " + selectedCountry.toString());
                }
            });

            return rootView;
        }
    }
}
