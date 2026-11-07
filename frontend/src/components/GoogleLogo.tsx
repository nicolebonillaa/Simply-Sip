import React from "react";
import Svg, { Path } from "react-native-svg";

const GoogleLogo = () => {
  return (
    <Svg width={22} height={22} viewBox="0 0 48 48">
      <Path
        fill="#4285F4"
        d="M43.6 24.5c0-1.4-.1-2.8-.4-4.1H24v7.8h11c-.5 2.5-1.9 4.6-4.1 6v5h6.6c3.9-3.6 6.1-8.7 6.1-14.7z"
      />
      <Path
        fill="#34A853"
        d="M24 44c5.5 0 10.1-1.8 13.5-4.8l-6.6-5c-1.8 1.2-4.1 2-6.9 2-5.3 0-9.8-3.6-11.4-8.4H5.8v5.1C9.2 39.6 16.1 44 24 44z"
      />
      <Path
        fill="#FBBC05"
        d="M12.6 27.8c-.4-1.2-.7-2.5-.7-3.8s.2-2.6.7-3.8v-5.1H5.8C4.4 17.8 3.6 20.8 3.6 24s.8 6.2 2.2 8.9l6.8-5.1z"
      />
      <Path
        fill="#EA4335"
        d="M24 9.6c3 0 5.7 1 7.8 3.1l5.8-5.8C34.1 3.6 29.5 2 24 2 16.1 2 9.2 6.4 5.8 15.1l6.8 5.1C14.2 13.2 18.7 9.6 24 9.6z"
      />
    </Svg>
  );
};

export default GoogleLogo;
