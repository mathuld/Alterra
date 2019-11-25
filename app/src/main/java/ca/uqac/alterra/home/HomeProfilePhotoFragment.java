package ca.uqac.alterra.home;

import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import ca.uqac.alterra.R;

public class HomeProfilePhotoFragment extends Fragment{


    private static String urlArgument = "url";
    private String mUrl;
    private ImageView imageToShow;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    public static HomeProfilePhotoFragment newInstance(String imageUrl){
        Bundle args = new Bundle();
        args.putString(urlArgument, imageUrl);
        HomeProfilePhotoFragment homeProfilePhotoFragment = new HomeProfilePhotoFragment();
        homeProfilePhotoFragment.setArguments(args);
        return homeProfilePhotoFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View fragmentView = inflater.inflate(R.layout.fragment_home_profile_photos,container,false);

        fragmentView.setOnTouchListener(new View.OnTouchListener() {

            PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
            PointF StartPT = new PointF(); // Record Start Position of 'img'

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {



                mScaleGestureDetector.onTouchEvent(motionEvent);


                Point size = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(size);
                float xClicked = 0f;
                float yClicked = 0f;

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        xClicked = motionEvent.getRawX();
                        yClicked = motionEvent.getRawY();

                        System.out.println("PRINT X CLICKED : " + xClicked);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if(mScaleFactor > 1.0f && !mScaleGestureDetector.isInProgress()){
                            view.animate().translationX(motionEvent.getRawX()-xClicked).setDuration(0).start();
                            view.animate().translationY(motionEvent.getRawY()-yClicked).setDuration(0).start();

                            xClicked = motionEvent.getRawX();
                            yClicked = motionEvent.getRawY();
                        }
                        break;
                }




                return true;
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(fragmentView.getContext(),new ScaleListener());
        return fragmentView ;
    }

    @Override
    public void onStart(){
        super.onStart();
        mUrl = getArguments().getString(urlArgument);

        imageToShow = (ImageView) getView().findViewById(R.id.imageToShow);

        Glide.with(getContext())
                .load(mUrl)
                .fitCenter()
                .into(imageToShow);

    }

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(1.0f,
                    Math.min(mScaleFactor, 10.0f));
            imageToShow.setScaleX(mScaleFactor);
            imageToShow.setScaleY(mScaleFactor);
            return true;
        }
    }
}
