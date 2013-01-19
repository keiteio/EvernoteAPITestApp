package jp.co.applicative.app.evernote;

import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.userstore.UserStore;
import com.evernote.thrift.protocol.TBinaryProtocol;
import com.evernote.thrift.transport.THttpClient;

public class Sample {

	private static final String userStoreUrl = "https://sandbox.evernote.com/edam/user";
	private static final String myUserAgent = "apitest";
	
	public static NoteStore.Client noteStore;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//String developerToken = "my developer token";
		String developerToken = "S=s1:U=57a45:E=143a99642e3:C=13c51e516e3:P=1cd:A=en-devtoken:H=2cb715a6dc88684aff00fdf61d67fded";

		try {
		// UserStore
		THttpClient userStoreTrans = new THttpClient(userStoreUrl);
		userStoreTrans.setCustomHeader("User-Agent", myUserAgent);
		TBinaryProtocol userStoreProt = new TBinaryProtocol(userStoreTrans);
		UserStore.Client userStore = new UserStore.Client(userStoreProt, userStoreProt);
	    String notestoreUrl = userStore.getNoteStoreUrl(developerToken);

	    // NoteStore
	    THttpClient noteStoreTrans = new THttpClient(notestoreUrl);
	    noteStoreTrans.setCustomHeader("User-Agent", myUserAgent);
	    TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
	    noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
