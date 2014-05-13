package changycj.anemiascan;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HistoryFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ListView rootView = (ListView) inflater.inflate(R.layout.fragment_history, container,
				false);
		((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
			.hideSoftInputFromWindow(rootView.getWindowToken(),0);
		
		Bundle args = getArguments();
		final String patientName = args.getString("patientName");
		final String patientId = args.getString("patientId");

		String path = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).toString() + File.separator 
				+ "AnemiaScan" + File.separator 
				+ String.format("%s_%s", patientId, patientName);
		File dir = new File(path);
		final File[] files = dir.listFiles();
		
	    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
		        android.R.layout.simple_list_item_1);
	    rootView.setAdapter(adapter);
	    
		if (files == null || files.length == 0) {
			adapter.add("No patient history available.");
		} else {
			
		    final ArrayList<String> list = new ArrayList<String>();
		    for (int i = 0; i < files.length; ++i) {
		      list.add(files[i].getName());
		    }

		    adapter.addAll(list);
		    
			AdapterView.OnItemClickListener listener = new OnItemClickListener () {

				@Override
				public void onItemClick(AdapterView<?> adapter, View view, int position,
						long id) {
					FragmentTransaction trans = getFragmentManager().beginTransaction();
					ViewFileFragment viewFile = new ViewFileFragment();
					trans.replace(R.id.container, viewFile);
					
					Bundle bundle = new Bundle();
					bundle.putString("filePath", files[position].getPath());
					bundle.putString("patientId", patientId);
					bundle.putString("patientName", patientName);
					viewFile.setArguments(bundle);
					trans.addToBackStack(null);
					trans.commit();
				}
			};
			rootView.setOnItemClickListener(listener);

		}
		return rootView;
	}
}
