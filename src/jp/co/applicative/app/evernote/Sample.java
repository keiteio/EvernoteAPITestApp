package jp.co.applicative.app.evernote;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Iterator;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.edam.userstore.UserStore;
import com.evernote.thrift.TException;
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
	    
	    // �e�X�g���{
	    TestMethod(developerToken, args[0], 0, 3);
	    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static void TestMethod(String developerToken, String imageFilepath, int count, int tryMax) throws Exception{
		 NoteList notes = findNotes(developerToken, "intitle:ImgRecogTest");
		    if(notes.getNotes().isEmpty()){
		    	// create a new note if it`s not found. 
		    	Note note = new Note();
		    	note.setTitle("ImgRecogTest");
		    	
		    	// Add an image resource.
		    	Resource resource = new Resource();
		    	resource.setData(readFileAsData(imageFilepath));
		    	resource.setMime("image/jpg");
		    	ResourceAttributes attributes = new ResourceAttributes();
		        attributes.setFileName(imageFilepath);
		        resource.setAttributes(attributes);
		        note.addToResources(resource);
		    	
		        String hashHex = bytesToHex(resource.getData().getBodyHash());
		    	String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		    	        + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
		    	        + "<en-note>" 
		    	        + "<span style=\"color:green;\">Here's the Evernote logo:</span><br/>"
		    	        + "<en-media type=\"image/png\" hash=\"" + hashHex + "\"/>"
		    	        + "</en-note>";
		    	note.setContent(content);
		    	
		    	// Upload the noat.
			    noteStore.createNote(developerToken, note);
			    
			    // wait three sec.
			    Thread.sleep(3000);
			    
			    // Try few times.
		    	if(count < tryMax){
		    		TestMethod(developerToken,imageFilepath, count + 1, tryMax);
		    	}
		    }else{
		    	for(Iterator<Note> itr = notes.getNotesIterator();itr.hasNext();){
		    		Note n = itr.next();
		    		for(Iterator<Resource> res_itr = n.getResourcesIterator();res_itr.hasNext();){
		    			Resource r = res_itr.next();
		    			
		    			byte[] imgRecogData = noteStore.getResourceRecognition(developerToken, r.getGuid());
		    			//Resource res = noteStore.getResource(developerToken, r.getGuid(), false, true, false, false);
		    			//byte[] imgRecogData = res.getRecognition().getBody();
		    			for(int i = 0;i<imgRecogData.length;i++){
		    				System.out.print(imgRecogData[i]);
		    				System.out.print("  ");
		    			}
		    			System.out.println("");
		    		}
		    	}
		    }
	}
	
	static NoteList findNotes(String authToken, NoteFilter filter) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException{
		return noteStore.findNotes(authToken, filter, 0, 50);
	}
	
	static NoteList findNotes(String authToken, String query) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException{
		NoteFilter filter = new NoteFilter();
	    filter.setWords(query);
	    filter.setOrder(NoteSortOrder.UPDATED.getValue());
	    filter.setAscending(false);
		return findNotes(authToken, filter);
	}
	
	  /**
	   * Helper method to read the contents of a file on disk and create a new Data object.
	   */
	  private static Data readFileAsData(String fileName) throws Exception {
	    // Read the full binary contents of the file
	    FileInputStream in = new FileInputStream(fileName);
	    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	    byte[] block = new byte[10240];
	    int len;
	    while ((len = in.read(block)) >= 0) {
	    	byteOut.write(block, 0, len);
	    }
	    in.close();
	    byte[] body = byteOut.toByteArray();
	    
	    // Create a new Data object to contain the file contents
	    Data data = new Data();
	    data.setSize(body.length);
	    data.setBodyHash(MessageDigest.getInstance("MD5").digest(body));
	    data.setBody(body);
	    
	    return data;
	  }
	  
	  
      /**
       * Helper method to convert a byte array to a hexadecimal string.
       **/
	  public static String bytesToHex(byte[] bytes) {
	    StringBuilder sb = new StringBuilder();
	    for (byte hashByte : bytes) {
	      int intVal = 0xff & hashByte;
	      if (intVal < 0x10) {
	        sb.append('0');
	      }
	      sb.append(Integer.toHexString(intVal));
	    }
	    return sb.toString();
	  }
}
