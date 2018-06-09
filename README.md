# RealtimeCircularProgessBar
*Android progress bar that updates with minimal delay.*

Typical implementations of progress bars in Android aren't well suited with displaying real-time data quickly since many
either:

1. Use animations to give an illustion of smoothness. This adds a delay to display frames within a given time, or
2. Call the invalidate() method in View which is slow since it waits on the shared Android UI thread

RealtimeCircularProgessBar was created for developers who want to display data as fast as possible on their phone screens.

## Installation

1. Add the JitPack repository to your build file. Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
2. Add the dependency:
```
	dependencies {
	        implementation 'com.github.dennis-kao:android-RealtimeCircularProgessBar:master-SNAPSHOT'
	}
```

## Usage
1. Within your XML layout file, include this:
```
        <de.dkao.rtcp.RealtimeCircularProgressBar
            android:id="@+id/progressBar"
            android:layout_width="250dp"
            android:layout_height="250dp"/>
```

2. Within the Activity/Fragment you are looking to use the progress bar in, make sure to include these two method calls
in the onResume and the onPause methods:
```
public class mActivity extends Activity {

  ...
  private RealtimeCircularProgressBar progressBar;
  ...
  
    @Override
    public void onResume() {
      super.onResume();
      ...
      progressBar.onResumeSurfaceView();
      ...
    }
    
    @Override
    public void onPause() {
        super.onPause();
        ...
        progressBar.onPauseSurfaceView();
        ...
    }
}

```

3. To set or update data use the method call setProgress():
```
int progress = 0;
int count = 0;

while(true) {
  progressBar.setProgress(progress, count);
  progress += 1;
  count += 1;
}
```

Code examples will soon be added to the repository.

## Implementation details
The RealtimeProgressBar class extends the Android SurfaceView class. SurfaceView enables frames to be displayed as fast as
the operating system allows by spawning a seperate Thread. This thread constantly redraws frames based off of parameters like
stroke width, mRoundedCorners, color etc.

The Thread is created when onResume() is called and is destroyed when onPause() is called in the Activity it resides in.
