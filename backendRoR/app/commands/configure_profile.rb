class ConfigureProfile
    prepend SimpleCommand

    def initialize(headers = {}, img, name, surname, gender, age, weight)
        @headers = headers
        @img = img
        @name = name
        @surname = surname
        @gender = gender
        @age = age
        @weight = weight
    end

    def call
        complete
    end

    private

    attr_accessor :headers

    def complete
        if check_token
            @user ||= user

            if @user
                @user.img = @img                if (not @img.nil?) and (not @img.empty?)
                @user.name = @name              if (not @name.nil?) and (not @name.empty?)
                @user.surname = @surname        if (not @surname.nil?) and (not @surname.empty?)
                @user.gender = @gender          if (not @gender.nil?) and (not @gender.empty?) and (@gender == 'M' or @gender == 'F')
                @user.age = @age                if (not @age.nil?) and (not @age.to_s.empty?) and @age.to_i > 0
                @user.weight = @weight          if (not @weight.nil?) and (not @weight.to_s.empty?) and @weight.to_f > 0
                @user.configure_token = nil
                @user.info_completed = true
                @user.save
            else
                errors.add(:user, 'Invalid token')
            end
        end
    end

    def user
        @user ||= User.find_by_configure_token(http_auth_header)                    #in case of one-time token
        @user ||= User.find(decoded_auth_token[:user_id]) if decoded_auth_token     #in case of jwt token
        @user || errors.add(:token, 'Invalid token') && nil
    end

    def check_token
        #Token not present
        if http_auth_header.nil?
            errors.add(:token, 'Missing token')
            return false
        end

        #One-time token
        if User.exists?(configure_token: http_auth_header)
            if BlacklistedToken.exists?(token: http_auth_header)
                errors.add(:token, 'Token in blacklist, please activate account before')
                return false
            else
                return true
            end
        #JWT token
        elsif decoded_auth_token and User.exists?(decoded_auth_token[:user_id])
            token = decoded_auth_token
            if BlacklistedToken.exists?(token: http_auth_header)
                errors.add(:token, 'Token in blacklist, please activate account before')
                return false
            elsif token[:exp] < Time.now.to_i
                errors.add(:token, 'Token in blacklist, please login again')
                return false
            else
                return true
            end
        #Invalid token
        else
            errors.add(:token, 'Token not valid')
        end

    end

    def decoded_auth_token
        @decoded_auth_token ||= JsonWebToken::decode(http_auth_header)
    end
  
    def http_auth_header

        if headers['Authorization'].present?
            return headers['Authorization'].split(' ').last
        else
            errors.add(:token, 'Missing token')
        end
        nil
    end
end