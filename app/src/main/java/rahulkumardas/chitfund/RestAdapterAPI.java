package rahulkumardas.chitfund;

import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;

import java.util.List;

import rahulkumardas.chitfund.ui.ChitFundApplication;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by Rahul Kumar Das on 01-05-2017.
 */

public interface RestAdapterAPI {
    public static final String BASE_END_POINT = ChitFundApplication.reference +"/"+ ChitFundApplication.USER+"/";

    //get the currentStamp and bSheetStamp object
    @GET("sheetStamp.json")
    Call<JsonObject> getCurrentStamp();

    //check for update in normal chit
    @GET("Chits/{id}.json")
    Call<JsonObject> checkUpdate(@Path("id") String id);
}
