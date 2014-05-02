package changycj.anemiascan;

import android.os.Bundle;
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

public class ResultFragment extends Fragment implements OnClickListener{

	private Button mainMenuButton;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_result, container,
				false);
		
		Bundle args = getArguments();
		double hemoCount = args.getDouble("hemoCount");
		String patientName = args.getString("patientName");
		String patientId = args.getString("patientId");
		
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

		return rootView;
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
