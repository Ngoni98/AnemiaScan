package changycj.anemiascan;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class SignInFragment extends Fragment implements OnClickListener {

	public Button startScanButton;
	public Button instructionsButton;

	public SignInFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_signin, container,
				false);
		
		startScanButton = (Button) rootView.findViewById(R.id.sign_in_start_scan);
		startScanButton.setOnClickListener(this);
		
		instructionsButton = (Button) rootView.findViewById(R.id.sign_in_instructions);
		instructionsButton.setOnClickListener(this);
		
		return rootView;
	}

	@Override
	public void onClick(View v) {
		FragmentTransaction trans = getFragmentManager().beginTransaction();

		switch(v.getId()) {
			case R.id.sign_in_start_scan:
				PrickFragment prick = new PrickFragment();
				trans.replace(R.id.container, prick);
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