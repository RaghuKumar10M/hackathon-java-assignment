package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class LegacyStoreManagerGateway {

  public void createStoreOnLegacySystem(StoreDetails dbStore) {
    // just to emulate as this would send this to a legacy system, let's write a temp file with the
    writeToFile(dbStore);
  }

  public void updateStoreOnLegacySystem(StoreDetails dbStore) {
    // just to emulate as this would send this to a legacy system, let's write a temp file with the
    writeToFile(dbStore);
  }

  private void writeToFile(StoreDetails dbStore) {
    try {
      // Step 1: Create a temporary file
      Path tempFile;

      tempFile = Files.createTempFile(dbStore.name(), ".txt");

      System.out.println("Temporary file created at: " + tempFile.toString());

      // Step 2: Write data to the temporary file
      String content =
          "Store created. [ name ="
              + dbStore.name()
              + " ] [ items on stock ="
              + dbStore.quantityProductsInStock()
              + "]";
      Files.write(tempFile, content.getBytes());
      System.out.println("Data written to temporary file.");

      // Step 3: Optionally, read the data back to verify
      String readContent = new String(Files.readAllBytes(tempFile));
      System.out.println("Data read from temporary file: " + readContent);

      // Step 4: Delete the temporary file when done
      Files.delete(tempFile);
      System.out.println("Temporary file deleted.");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
