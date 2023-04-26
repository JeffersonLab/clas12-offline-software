package org.jlab.rec.ahdc.Distance;

import org.jlab.rec.ahdc.Cluster.Cluster;
import org.jlab.rec.ahdc.Track.Track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Distance {

    private ArrayList<Track> _AHDCTracks;

    public Distance(){
        _AHDCTracks = new ArrayList<>();
    }

    public void find_track(List<Cluster> AHDC_Cluster){
        find_track_4_clusters(AHDC_Cluster);
        find_track_3_clusters(AHDC_Cluster);
    }

    public static <T> List<List<T>> computeCombinations2(List<List<T>> lists) {
        List<List<T>> combinations = Arrays.asList(Arrays.asList());
        for (List<T> list : lists) {
            List<List<T>> extraColumnCombinations = new ArrayList<>();
            for (List<T> combination : combinations) {
                for (T element : list) {
                    List<T> newCombination = new ArrayList<>(combination);
                    newCombination.add(element);
                    extraColumnCombinations.add(newCombination);
                }
            }
            combinations = extraColumnCombinations;
        }
        return combinations;
    }

    private void find_track_4_clusters(List<Cluster> AHDC_Cluster){
        List<Cluster> clusters_to_remove = new ArrayList<>();
        List<Cluster> layer1 = new ArrayList<>(); // List of all cluster with a radius equal 35
        List<Cluster> layer2 = new ArrayList<>(); // List of all cluster with a radius equal 45
        List<Cluster> layer3 = new ArrayList<>(); // List of all cluster with a radius equal 55
        List<Cluster> layer4 = new ArrayList<>(); // List of all cluster with a radius equal 65

        for(Cluster cluster : AHDC_Cluster){
            if(cluster.get_Radius() == 35){
                layer1.add(cluster);
            }
            else if(cluster.get_Radius() == 45){
                layer2.add(cluster);
            }
            else if(cluster.get_Radius() == 55){
                layer3.add(cluster);
            }
            else if(cluster.get_Radius() == 65){
                layer4.add(cluster);
            }
        }
        List<List<Cluster>> merged_list = new ArrayList<>();
        merged_list.add(layer1);
        merged_list.add(layer2);
        merged_list.add(layer3);
        merged_list.add(layer4);
        List<List<Cluster>> all_combinations = computeCombinations2(merged_list);

        List<Track> all_track = new ArrayList<>();
        for(List<Cluster> combination : all_combinations){
            all_track.add(new Track(combination));
        }

        List<Track> tracks_possible = new ArrayList<>();
        for(Track track : all_track){
            if(track.get_Distance() < 45){
                tracks_possible.add(track);
            }
        }

        double window = 3.8;
        for(Track track : tracks_possible){
            List<Track> tracks_with_close_starting_point = new ArrayList<>();
            for(Track other_track : tracks_possible){
                if(other_track.get_Clusters().get(0).get_X() > track.get_Clusters().get(0).get_X() - window
                    && other_track.get_Clusters().get(0).get_X() < track.get_Clusters().get(0).get_X() + window
                    && other_track.get_Clusters().get(0).get_Y() > track.get_Clusters().get(0).get_Y() - window
                    && other_track.get_Clusters().get(0).get_Y() < track.get_Clusters().get(0).get_Y() + window
                    && !other_track.is_Used()){
                    tracks_with_close_starting_point.add(other_track);
                    other_track.set_Used(true);
                }
            }

            if(tracks_with_close_starting_point.size() > 0){
                double chisq_min = Double.MAX_VALUE;
                Track best_track = null;
                for(Track other_track : tracks_with_close_starting_point){
                    ArrayList<Double> x_ = new ArrayList<>();
                    ArrayList<Double> y_ = new ArrayList<>();
                    ArrayList<Double> w_ = new ArrayList<>(); // weight for circlefit
                    for(Cluster cluster : other_track.get_Clusters()){
                        x_.add(cluster.get_X());
                        y_.add(cluster.get_Y());
                        w_.add(1.);
                    }

                    CircleFitter circlefitter = new CircleFitter();
                    if(circlefitter.fitStatus(x_,y_,w_,x_.size())){
                        double chisq = Math.abs(circlefitter.getFit().chisq() - 1);
                        if(chisq < chisq_min){
                            chisq_min = chisq;
                            best_track = other_track;
                        }
                    }
                }
                if (best_track != null ){
                    clusters_to_remove.addAll(best_track.get_Clusters());
                    _AHDCTracks.add(best_track);
                }
            }
        }

        List<Cluster> clusters_to_remove_without_double = new ArrayList<>();
        for(Cluster cluster : clusters_to_remove){
            if(!containsCluster(clusters_to_remove_without_double, cluster.get_Phi(), cluster.get_Radius())){
                clusters_to_remove_without_double.add(cluster);
            }
        }

        for(Cluster cluster : clusters_to_remove_without_double){
            AHDC_Cluster.remove(cluster);
        }
    }

    public boolean containsCluster(final List<Cluster> list, double phi, double radius){
        return list.stream().anyMatch(o -> o.get_Radius() == (radius) && o.get_Phi() == phi);
    }


   private ArrayList<ArrayList<Cluster>> combination(List<Cluster> arr, ArrayList<Cluster> data, int start,
                                           int end, int index, int r) {

        ArrayList<ArrayList<Cluster>> all = new ArrayList<>();
        if (index == r) {
            all.add(data);
        }

        for (int i=start; i<=end && end-i+1 >= r-index; i++) {
            data.add(arr.get(i));
            combination(arr, data, i+1, end, index+1, r);
        }

        return all;
    }

    private void find_track_3_clusters(List<Cluster> AHDC_Cluster){
        ArrayList<ArrayList<Cluster>> all_combinations = combination(AHDC_Cluster, new ArrayList<Cluster>(),0, AHDC_Cluster.size()-1, 0, 3);

        List<Track> all_track = new ArrayList<>();
        for(List<Cluster> combination : all_combinations){
            all_track.add(new Track(combination));
        }

        List<Track> tracks_possible = new ArrayList<>();
        for(Track track : all_track){
            if(track.get_Distance() < 45){
                tracks_possible.add(track);
            }
        }

        double window = 3.8;
        for(Track track : tracks_possible) {
            List<Track> tracks_with_close_starting_point = new ArrayList<>();
            for (Track other_track : tracks_possible) {
                if (other_track.get_Clusters().get(0).get_X() > track.get_Clusters().get(0).get_X() - window
                        && other_track.get_Clusters().get(0).get_X() < track.get_Clusters().get(0).get_X() + window
                        && other_track.get_Clusters().get(0).get_Y() > track.get_Clusters().get(0).get_Y() - window
                        && other_track.get_Clusters().get(0).get_Y() < track.get_Clusters().get(0).get_Y() + window
                        && !other_track.is_Used()) {
                    tracks_with_close_starting_point.add(other_track);
                    other_track.set_Used(true);
                }
            }

            if(tracks_with_close_starting_point.size() > 0){
                double chisq_min = Double.MAX_VALUE;
                Track best_track = null;
                for(Track other_track : tracks_with_close_starting_point){
                    ArrayList<Double> x_ = new ArrayList<>();
                    ArrayList<Double> y_ = new ArrayList<>();
                    ArrayList<Double> w_ = new ArrayList<>(); // weight for circlefit
                    for(Cluster cluster : other_track.get_Clusters()){
                        x_.add(cluster.get_X());
                        y_.add(cluster.get_Y());
                        w_.add(1.);
                    }

                    CircleFitter circlefitter = new CircleFitter();
                    if(circlefitter.fitStatus(x_,y_,w_,x_.size())){
                        double chisq = Math.abs(circlefitter.getFit().chisq() - 1);
                        if(chisq < chisq_min){
                            chisq_min = chisq;
                            best_track = other_track;
                        }
                    }
                }
                if (best_track != null ){
                    _AHDCTracks.add(best_track);
                }
            }
        }
    }

    public ArrayList<Track> get_AHDCTracks() {
        return _AHDCTracks;
    }

    public void set_AHDCTracks(ArrayList<Track> _AHDCTracks) {
        this._AHDCTracks = _AHDCTracks;
    }
}
