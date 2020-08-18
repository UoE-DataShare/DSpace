/**
 * Add title to each option of the collection
 * in the dropdown.
 */

(function() {
   "use strict";
   
   // Add title for each item in dropdown
   $('#aspect_administrative_item_MoveItemForm_field_collectionID')
     .children('option')
     .each(function () {
             $(this).attr('title', $(this).text());
   });

})();
