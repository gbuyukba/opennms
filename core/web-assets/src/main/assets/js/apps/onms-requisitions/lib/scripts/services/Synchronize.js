const bootbox = require('bootbox');

require('./Requisitions');

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc service
  * @name SynchronizeService
  * @module onms-requisitions
  *
  * @requires RequisitionsService The requisitions service
  * @requires growl The growl plugin for instant notifications
  *
  * @description The SynchronizeService provides a way to request a requisition synchronization asking the user how the scan process will be processed.
  */
  .factory('SynchronizeService', ['RequisitionsService', 'growl', function(RequisitionsService, growl) {
    return {
      /**
      * @description Requests the synchronization/import of a requisition on the server
      *
      * A dialog box is displayed to request to the user if the scan phase should be triggered or not.
      *
      * @name SynchronizeService:synchronize
      * @ngdoc method
      * @methodOf SynchronizeService
      * @param {object} requisition The requisition object
      * @param {function} successHandler The function to call after a successful synchronization
      * @param {function} errorHandler The function to call when something went wrong.
      */
      synchronize: function(requisition, errorHandler) {
        /**
        * @param {object} requisition The requisition object
        * @param {string} rescanExisting true to perform a full scan, false to only add/remove nodes without scan, dbonly for all DB operations without scan
        */
        var doSynchronize = function(requisition, rescanExisting) {
          RequisitionsService.startTiming();
          RequisitionsService.synchronizeRequisition(requisition.foreignSource, rescanExisting).then(
            function() { // success
              growl.success('The import operation has been started for ' + requisition.foreignSource + ' (rescanExisting? ' + rescanExisting + ')<br/>Use <b>refresh</b> to update the deployed statistics');
              requisition.setDeployed(true);
            },
            errorHandler
          );
        };
        bootbox.dialog({
          message: '<b>Rescan Options</b>: <br/><hr/>' +
                   '<b>Full (true)</b>: synchronize and scan all nodes (new and existing).<br/>' +
                   '<b>Partial (false)</b>: synchronize only new and deleted nodes. Run the scan phase only for new nodes.<br/>' +
                   '<b>DB Only (dbonly)</b>: synchronize all nodes, skip the scan phase.<br/>' ,
          
          title: 'Synchronize Requisition ' + requisition.foreignSource,
          buttons: {
            fullSync: {
              label: 'Full',
              className: 'btn-primary',
              callback: function() {
                doSynchronize(requisition, 'true');
              }
            },
            ignoreExistingSync: {
              label: 'Partial',
              className: 'btn-secondary',
              callback: function() {
                doSynchronize(requisition, 'false');
              }
            },
            dbOnlySync: {
              label: 'DB Only',
              className: 'btn-secondary',
              callback: function() {
                doSynchronize(requisition, 'dbonly');
              }
            },
            main: {
              label: 'Cancel',
              className: 'btn-secondary'
            }
          }
        });
      }
    };
  }]);

}());
