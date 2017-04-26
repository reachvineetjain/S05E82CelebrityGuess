package com.nehvin.s05e82celebrityguess;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Button btnChoice1 = null;
    Button btnChoice2 = null;
    Button btnChoice3 = null;
    Button btnChoice4 = null;
    ImageView celebImageView = null;
    HashMap<String,Bitmap> celeb = new HashMap<>();
    ArrayList<String> celebNameList = new ArrayList<>();
    Random rand = new Random();
    boolean playOn = true;
    Set keySet;
    ArrayList<String> celebNamesList;
    int celebToGuess;
    List<Integer> answers = new ArrayList();
    int locationOfCorrectAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
//        getWebContent("http://www.posh24.se/kandisar");
//http://www.autocarbrands.com/car-logos/
//
        DownloadWebContentTask downloadWebContent = new DownloadWebContentTask();
        String result = "";

        try {
            result = downloadWebContent.execute("http://www.posh24.se/kandisar").get();
        } catch (InterruptedException e) {
            Log.e("error","unable to download content");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.e("error","unable to download content");
            e.printStackTrace();
        }
//        Log.i("info", result);
        // extract image and name from the above result
        extractImageAndName(result);
        Log.i("info","Hashmap count"+celeb.size());
    }

    private void extractImageAndName(String htmlPage)
    {
        Pattern imgPattern = Pattern.compile("<img src=\"(.*?)\" alt=");
        Matcher imgMatcher = imgPattern.matcher(htmlPage);
        Pattern celebPattern = Pattern.compile("\" alt=\"(.*?)\"/>");
        Matcher celebMatcher = celebPattern.matcher(htmlPage);
        int i = 0;

        StringBuffer celebImgString = new StringBuffer("");
        ImageDownloadTask imgDnlTask = new ImageDownloadTask();

        while(imgMatcher.find() && celebMatcher.find())
        {
//            Log.i("Celeb url", imgMatcher.group(1));
            celebImgString.append(imgMatcher.group(1)).append(",");
            celebNameList.add(celebMatcher.group(1));
//            Log.i("Celeb Name", celebMatcher.group(1));
            Log.i("Celeb No", Integer.toString(++i));
        }

        String[] strUrls = celebImgString.toString().split(",");
        Log.i("info", strUrls.toString());

        try {
            imgDnlTask.execute(strUrls).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        keySet = celeb.keySet();
        celebNamesList = new ArrayList<String>(keySet);

        startGuessGame();

        //        Set keySet = celeb.keySet();
//        Iterator<String> str = keySet.iterator();
//        String tempString;
//        Bitmap tempBmp;
//        ArrayList<String> celebNamesList =
//        while(str.hasNext())
//        {
//            tempString=str.next();
//            Log.i("info",tempString);
//            tempBmp = celeb.get(tempString);
//            celebImageView.setImageBitmap(tempBmp);
//        }
//
    }

    private void startGuessGame()
    {
        celebToGuess = (int) (rand.nextInt(celebNamesList.size()) + 1);
        String celebName = celebNamesList.get(celebToGuess);
        locationOfCorrectAnswer = rand.nextInt(4);
        int incorrectAnswer;

        for (int i=0;i<4;i++)
        {
            if(i == locationOfCorrectAnswer)
            {
                answers.add(i,celebToGuess);
            }
            else
            {
                incorrectAnswer = rand.nextInt(celebNamesList.size())+1;
                while(incorrectAnswer == celebToGuess)
                {
                    incorrectAnswer = rand.nextInt(celebNamesList.size())+1;
                }
                answers.add(i,incorrectAnswer);
            }
        }
        Bitmap tempBmp = celeb.get(celebName);
        celebImageView.setImageBitmap(tempBmp);
        btnChoice1.setText(celebNamesList.get(answers.get(0)));
        btnChoice2.setText(celebNamesList.get(answers.get(1)));
        btnChoice3.setText(celebNamesList.get(answers.get(2)));
        btnChoice4.setText(celebNamesList.get(answers.get(3)));
    }

    public void checkAnswer(View view)
    {
        if(Integer.parseInt(view.getTag().toString()) == locationOfCorrectAnswer)
        {
            Toast.makeText(getApplicationContext(), "Correct Answer!!", Toast.LENGTH_SHORT).show();
//            answerTextView.setText("Correct Answer!!");
//            totalCorrectAnswers++;
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Incorrect Answer!! It is "+celebNamesList.get(celebToGuess), Toast.LENGTH_SHORT).show();
//            answerTextView.setText("Wrong Answer!!!");
        }
        startGuessGame();
//        totalAnswered++;
//        scoreCountView.setText(totalCorrectAnswers+"/"+totalAnswered);
//        while(playOn)
//        {
//            startGuessGame();
//        }
    }

//    public void stopGame(View view)
//    {
//        playOn = false;
//    }

    private void initialize ()
    {
        celebImageView = (ImageView) findViewById(R.id.celebImageView);
        btnChoice1 = (Button) findViewById(R.id.btnChoice1);
        btnChoice2 = (Button) findViewById(R.id.btnChoice2);
        btnChoice3 = (Button) findViewById(R.id.btnChoice3);
        btnChoice4 = (Button) findViewById(R.id.btnChoice4);
     }

    class ImageDownloadTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i("info","Inside download task");
            URL url = null;
            HttpURLConnection connection = null;
            InputStream stream = null;
            Bitmap bmpMap = null;
            try {
                for(int i = 0; i < celebNameList.size() ; i++)
                {
                    url = new URL(params[i]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    Log.i("info", "Connection completed");
                    stream = connection.getInputStream();
                    Log.i("info", "stream obtained");
                    bmpMap = BitmapFactory.decodeStream(stream);
                    celeb.put(celebNameList.get(i),bmpMap);
                    Log.i("info", "Stream decoded and converted to bitmap");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if(stream != null)
                        stream.close();
                    if(connection != null)
                        connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    class DownloadWebContentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            InputStream in = null;
            InputStreamReader reader = null;
            try {
                Log.i("URL that has to be ", params[0]);
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                in = urlConnection.getInputStream();
                reader = new InputStreamReader(in);
                int data = reader.read();
                Log.i("info", "Starting to write data");
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                Log.i("info", "Finished writing data");
            } catch (IOException e) {
                Log.e("error","unable to download content");
                e.printStackTrace();
            }
            finally {
                try {
                    if(in != null)
                        in.close();
                    if(reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }
        }
    }
}