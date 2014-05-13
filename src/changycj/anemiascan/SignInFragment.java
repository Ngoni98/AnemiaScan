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
import android.widget.EditText;

public class SignInFragment extends Fragment implements OnClickListener {

	public Button startScanButton;
	public Button instructionsButton;
	public Button historyButton;

	public SignInFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_signin, container,
				false);
		
		startScanButton = (Button) rootView.findViewById(R.id.sign_in_start_scan);
		startScanButton.setOnClickListener(this);
		
		historyButton = (Button) rootView.findViewById(R.id.sign_in_history);
		historyButton.setOnClickListener(this);
		
		instructionsButton = (Button) rootView.findViewById(R.id.sign_in_instructions);
		instructionsButton.setOnClickListener(this);
		
		FragmentManager manager = getFragmentManager();
		manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		EditText name = (EditText) getView().findViewById(R.id.sign_in_patient_name);
		name.setText("");
		EditText id = (EditText) getView().findViewById(R.id.sign_in_patient_id);
		id.setText("");
	}
	
	@Override
	public void onClick(View v) {
		FragmentTransaction trans = getFragmentManager().beginTransaction();

		String name, id;
		switch(v.getId()) {
			case R.id.sign_in_start_scan:
				name = ((EditText) getView().findViewById(R.id.sign_in_patient_name)).getText()
						.toString().trim();
				id = ((EditText) getView().findViewById(R.id.sign_in_patient_id)).getText()
						.toString().trim();
				
				if (!name.isEmpty() && !id.isEmpty()) {
					PrickFragment prick = new PrickFragment();
					trans.replace(R.id.container, prick);
					
					Bundle bundle = new Bundle();
					bundle.putString("patientName", name);
					bundle.putString("patientId", id);
					prick.setArguments(bundle);
				}
				
				break;
			case R.id.sign_in_history:
				name = ((EditText) getView().findViewById(R.id.sign_in_patient_name)).getText()
						.toString().trim();
				id = ((EditText) getView().findViewById(R.id.sign_in_patient_id)).getText()
						.toString().trim();
				
				if (!name.isEmpty() && !id.isEmpty()) {
					HistoryFragment history = new HistoryFragment();
					trans.replace(R.id.container, history);
					
					Bundle bundle = new Bundle();
					bundle.putString("patientName", name);
					bundle.putString("patientId", id);
					history.setArguments(bundle);
					Log.d("SignInFragment", "calling history");
				}
				break;
				
			case R.id.sign_in_instructions:
				InstructionsFragment instructions = new InstructionsFragment();
				trans.replace(R.id.container, instructions);
				break;
		}
		trans.addToBackStack(null);
		trans.commit();

		
	}
	
}