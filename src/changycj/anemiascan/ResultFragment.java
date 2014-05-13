package changycj.anemiascan;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVWriter;

public class ResultFragment extends Fragment implements OnClickListener{

	private Button mainMenuButton;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_result, container,
				false);
		
		Bundle args = getArguments();
		double hemoCount = args.getDouble("hemoCount");
		String[] hemoPixels = args.getStringArray("hemoPixels");
		String patientName = args.getString("patientName");
		String patientId = args.getString("patientId");
		String hemoMeasure = args.getString("hemoMeasure");
		
		TextView name = (TextView) rootView.findViewById(R.id.result_patient_info);
		name.setText(String.format("%s - #%s", patientName, patientId));
		
		TextView measurement = (TextView) rootView.findViewById(R.id.result_measurement);
		measurement.setText(String.format("%.3f g/dl", hemoCount));

		String diagnosis;		
		if (hemoCount > 16.0) diagnosis = "Test again";
		else if (hemoCount > 12.0) diagnosis = "No Anemia";
		else if (hemoCount > 8.0) diagnosis = "Mild Anemia";
		else if (hemoCount > 6.0) diagnosis = "Moderate Anemia";
		else if (hemoCount > 4.0) diagnosis = "Severe Anemia";
		else if (hemoCount > 2.0) diagnosis = "Critical Anemia";
		else diagnosis = "Test again";
		
		TextView diag = (TextView) rootView.findViewById(R.id.result_diagnosis);
		diag.setText(diagnosis);
		
		mainMenuButton = (Button) rootView.findViewById(R.id.result_main_menu);
		mainMenuButton.setOnClickListener(this);
		
		saveData(patientId, patientName, hemoPixels, hemoCount, hemoMeasure);
		
		return rootView;
	}
	
	public void saveData(String patientId, String patientName, 
			String[] hemoPixels, double hemoCount, String hemoMeasure) {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			
			// try to create main directory
			File mainDir = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS), "AnemiaScan");			
			if (!mainDir.mkdirs()) {
				Log.e("ResultFragment", "Main directory not created");
			}
			
			// try to create patient directory
			File patientDir = new File(mainDir, String.format("%s_%s", patientId, patientName));
			if (!patientDir.mkdirs()) {
				Log.e("ResultFragment", "Patient directory not created");
			}
			
			// create the file
			// first make sure directory exists
			if (mainDir.exists() && patientDir.exists()) {
				Log.d("ResultFragment", "yay successful!");
		        String date = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
				String filename = patientDir.getPath() + File.separator + 
						patientId + "_" + date + ".csv";
				
				try {
					CSVWriter writer = new CSVWriter(new FileWriter(filename));
					writer.writeNext(new String[] {"HemoCount", "Red", "Green", "Blue"});
					for (String pixelString : hemoPixels) {
						writer.writeNext(pixelString.split("#"));
					}
					writer.writeNext(hemoMeasure.split("#"));
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("ResultFragment", "uh oh :(");
				}

			} else {
				Log.e("ResultFragment", "oh no :(");
			}
		}
	}
	
	
	@Override
	public void onClick(View v) {
		SignInFragment signin = new SignInFragment();
		
		FragmentManager manager = getFragmentManager();
		FragmentTransaction trans = manager.beginTransaction();
		trans.replace(R.id.container, signin);
		trans.addToBackStack(null);
		trans.commit();
	}
	
}
