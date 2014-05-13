package changycj.anemiascan;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Formatter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVReader;

public class ViewFileFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_viewfile, container,
				false);
		
		Bundle args = getArguments();
		String patientName = args.getString("patientName");
		String patientId = args.getString("patientId");
		String filePath = args.getString("filePath");
		
		TextView id = (TextView) rootView.findViewById(R.id.viewfile_patient_id);
		id.setText("ID: #" + patientId);
		
		TextView name = (TextView) rootView.findViewById(R.id.viewfile_patient_name);
		name.setText("Name: " + patientName);
		
		String date = filePath.substring(filePath.lastIndexOf("_") + 1, filePath.lastIndexOf("."));
		TextView dateText = (TextView) rootView.findViewById(R.id.viewfile_date);
		dateText.setText("Date: " + String.format("%s/%s/%s %s:%s:%s", 
				date.substring(0, 4), date.substring(4, 6), date.substring(6, 8),
				date.substring(8, 10), date.substring(10, 12), date.substring(12)));
		
		String calib = "";
		try {
			CSVReader reader = new CSVReader(new FileReader(filePath));
			String[] row = null;
			while ((row = reader.readNext()) != null) {
				calib += new Formatter().format("%s, %s, %s, %s \n", row[0], row[1], row[2], row[3]);
			}
			reader.close();
			
			TextView calibText = (TextView) rootView.findViewById(R.id.viewfile_calibration);
			calibText.setText(calib);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e("ViewFileFragment", "Error reading csv");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("ViewFileFragment", "Error reading csv");
		}
		
		return rootView;
	}
}
