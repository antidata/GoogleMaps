angular.module('gml3', ['ui.bootstrap']);
function TypeaheadCtrl($scope) {

  $scope.selected = undefined;
  $scope.lista = [];
  $scope.placedetail = [];


  $scope.getList = function(term) {
    var promise = pageFunctions.findPlaces(term); // call to lift function

    return promise.then(function(data) {
      $scope.$apply(function() {
        $scope.lista = data;
      })
      return data;
    });
  };


  $scope.placeDet = function(term) {
    var promise = pageFunctions.placeDetail(term); // call to lift function

    return promise.then(function(data) {
      $scope.$apply(function() {
        $scope.placedetail = data;
      })
      return data;
    });
  };

  $scope.setOnMap = function(lat, long) {
    mapInstance.setCenter(new google.maps.LatLng(lat,long));
  }
}