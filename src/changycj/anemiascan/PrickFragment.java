package changycj.anemiascan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class PrickFragment extends Fragment implements OnClickListener{
	private Button prickNextButton;
	private final static int CAMERA_ACTIVITY_REQUEST_CODE = 1;
	private final String TAG = "PrickFragment";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_prick, container,
				false);
		
		prickNextButton = (Button) rootView.findViewById(R.id.prick_next);
		prickNextButton.setOnClickListener(this);
		
		return rootView;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				double count = data.getDoubleExtra("hemoCount", 0.0);
				Bundle bundle = new Bundle();
				bundle.putDouble("hemoCount", count);
				
				ResultFragment result = new ResultFragment();
				result.setArguments(bundle);
				
				FragmentTransaction trans = getFragmentManager().beginTransaction();
				trans.replace(R.id.container, result);
				trans.addToBackStack(null);
				trans.commit();
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this.getActivity(), CameraActivity.class);
		startActivityForResult(intent, CAMERA_ACTIVITY_REQUEST_CODE);
	}
}
