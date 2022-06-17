/// Gets value that matches borders
///
/// # Parameters
/// **v** - value itself
///
/// **min** - minimum
///
/// **max** - maximum
///
/// # Returns
/// **v* itself if it's in [min..max] or min / max if not

#[macro_export]
macro_rules! get_in_borders {
    ($v:tt, $min: tt, $max: tt, $min_path:path, $max_path:path) => {
        $min_path($max, $max_path($min, $v))
    };
}
